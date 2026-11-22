# Giải trình và hướng dẫn cho QA

## QA dựa vào đâu để cho đạt?

Một case đạt khi kết quả thực tế khớp expected đã xác định từ yêu cầu, có bằng chứng và môi trường tái hiện được. Không cho đạt chỉ vì không thấy lỗi trên màn hình, code có test hoặc Maven báo build thành công.

Với mỗi case, ghi: commit, môi trường, trạng thái dữ liệu ban đầu, bước làm, expected, actual, kết luận và bằng chứng. Dùng Đạt, Không đạt, Bị chặn hoặc Chưa chạy. Test bị bỏ qua không được tính là Đạt.

## Chuẩn bị môi trường

Cần JDK 25, Docker Desktop và database demo của project. Không tạo thêm database hoặc container MySQL cho quy trình QA này. Ghi phiên bản từ `.\mvnw.cmd -version`, `docker version` và commit từ `git rev-parse HEAD`.

Database QA duy nhất là `bluemoon` do Docker Compose của project quản lý ở cổng `3307`. Khi tạo volume lần đầu, Docker chạy `src/main/resources/db/schema.sql` rồi `database/demo-data.sql`. Các test MySQL phải chạy trên database demo này sau khi đã sao lưu hoặc xác nhận có thể dọn dữ liệu thử.

```powershell
$env:BLUEMOON_IT_DB_URL = 'jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_IT_DB_USER = 'bluemoon_app'
$env:BLUEMOON_IT_DB_PASSWORD = 'bluemoon_dev'
$env:BLUEMOON_IT_EXISTING_DB = 'true'
.\mvnw.cmd '-Dtest=MySqlBillingFlowTest#existingDatabaseRateAndDuplicatePaymentRegression' test
Remove-Item Env:BLUEMOON_IT_EXISTING_DB, Env:BLUEMOON_IT_DB_URL, Env:BLUEMOON_IT_DB_USER, Env:BLUEMOON_IT_DB_PASSWORD
```

Kiểm tra không cần MySQL:

```powershell
.\mvnw.cmd "-Dtest=AuthorizationTest,BillingRulesTest,ResidenceChangeServiceTest,CsvExporterTest,FeeNoticePrinterTest,DemoDataSqlTest" test
```

Lệnh trên chỉ chạy các lớp được chọn. `DemoDataSqlTest` kiểm tra nội dung file SQL, không xác minh việc thực thi SQL trên MySQL. Xem số lượng thực tế trong báo cáo Surefire của từng lần chạy.

## Kiểm tra trên database demo

Khởi động đúng database của project và chạy test hồi quy trên dữ liệu demo:

```powershell
docker compose up -d mysql
$env:BLUEMOON_IT_DB_URL = 'jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_IT_DB_USER = 'bluemoon_app'
$env:BLUEMOON_IT_DB_PASSWORD = 'bluemoon_dev'
$env:BLUEMOON_IT_EXISTING_DB = 'true'
.\mvnw.cmd '-Dtest=MySqlBillingFlowTest#existingDatabaseRateAndDuplicatePaymentRegression' test
Remove-Item Env:BLUEMOON_IT_EXISTING_DB, Env:BLUEMOON_IT_DB_URL, Env:BLUEMOON_IT_DB_USER, Env:BLUEMOON_IT_DB_PASSWORD
```

Test này tự tạo và dọn bản ghi thử. Không chạy đồng thời với người đang dùng database demo và không dùng `docker compose down -v`, vì lệnh đó xóa volume dữ liệu.

| Bước | Thao tác | Expected và bằng chứng |
|---|---|---|
| Khởi động | `docker compose up -d mysql`, mở app và đăng nhập tài khoản trong `demo-data.sql` | Ứng dụng dùng database `bluemoon`; không tạo database khác. |
| Đăng nhập và quyền | Kiểm tra lần lượt tài khoản quản trị, kế toán và cư dân mẫu | Mỗi role thấy đúng menu và phạm vi dữ liệu; lưu ảnh màn hình. |
| Dữ liệu demo | Tìm các căn A101, A204 và các kỳ đã có trong database | Mã căn, khoản thu, thanh toán và công nợ khớp dữ liệu MySQL; lưu truy vấn hoặc ảnh đối chiếu. |
| Hồi quy thanh toán | Chạy `MySqlBillingFlowTest#existingDatabaseRateAndDuplicatePaymentRegression` | Chỉ một phiếu được ghi khi gửi trùng; đơn giá đầu kỳ được dùng; bản ghi thử được dọn sau test. |
| Đối soát | Mở bảng kê, công nợ, tổng quan và lịch sử thu | Tổng tiền trên UI khớp truy vấn read-only trên database demo. |
| In/CSV | Xem thông báo, in hoặc xuất CSV từ dữ liệu demo | Đúng hộ, khoản, kỳ, tiền và thông tin chuyển khoản; lưu bằng chứng. |
| UI | Tab/Enter, lỗi nhập, bảng rỗng, dữ liệu dài, cửa sổ 1080 × 700 và 1280 × 820 | Nhãn đọc được, focus rõ, menu cuộn tới cuối, không mất nút. |

Case nhận tiền phải kiểm tra số dòng trong `payments`, giá trị `applied_amount`, số dư và các charge liên quan. Ảnh toast thành công không đủ chứng minh số liệu đúng.

Phương tiện, tiện ích, yêu cầu cư dân, đóng góp và cư trú có case riêng trong [test case chức năng](QA_TEST_CASE_FUNCTIONAL.md). Hóa đơn tiện ích phải bị chặn sửa/xóa sau khi đã phát hành; đóng góp không tạo công nợ; biển số đang hoạt động không được trùng.

## Cách đọc kết quả

[Biên bản kiểm thử](QA_TEST_REPORT.md) có thể còn ghi các lượt chạy lịch sử trên schema riêng. Những số liệu đó không phải kết quả của quy trình hiện tại. Kết quả được chấp nhận hiện tại phải ghi rõ đã chạy trên database demo `bluemoon` và được tạo từ `schema.sql` cùng `demo-data.sql`.

Lượt JDK 21 trước đó dừng khi compile nên không có kết quả test. Lượt khởi động JavaFX với database rỗng chỉ xác nhận khởi động không exception, chưa chứng minh mọi thao tác UI hoặc in đạt.

## Những trường hợp không được cho qua

| Case | Kết luận hiện tại |
|---|---|
| Gửi lại cùng yêu cầu thu một phần hoặc gửi đồng thời | Dùng cùng mã giao dịch; phải chỉ có một phiếu trong DB. Case MySQL ngày 25/11 đạt; hai mã khác nhau không được nhận diện là cùng giao dịch. |
| Giá tăng giữa tháng, phát hành sau ngày tăng | Theo yêu cầu dùng giá ngày đầu kỳ. Case MySQL ngày 25/11 đạt; cần chạy lại khi thay đổi tính phí. |
| Đổi mã căn/tên hộ rồi in lại chứng từ cũ | R-04 còn mở; kiểm tra sự thay đổi và đối chiếu chính sách giữ lịch sử. |
| Sửa tên liên hệ khác với chủ hộ | R-05 còn mở; không tự kết luận hai trường là cùng ý nghĩa. |
| MySQL chậm, nhấn thao tác nhiều lần, in cả kỳ | R-06 chưa có kết quả đủ để kết luận; cần kiểm tra phản hồi và dữ liệu ghi lặp. |

QA có thể cho đạt từng case đã có bằng chứng. Kết luận toàn bộ bản dựng cần đối chiếu yêu cầu và lỗi chặn còn mở với PM.

## Trả lời khi được hỏi “vì sao cho pass?”

Nêu mã case, dữ liệu đầu vào, expected tính độc lập, actual đối chiếu và nơi lưu bằng chứng. Ví dụ: “Căn 48 m² × 10.000 đồng = 480.000 đồng; bảng kê và DB đều ghi 480.000 đồng, chỉ có một charge cho căn/phí/kỳ. Case tính phí này đạt. Case giá giữa tháng được theo dõi riêng và chưa đạt.”

Không đưa ví dụ này vào báo cáo như kết quả đã chạy nếu chưa thực hiện trên bản dựng đang kiểm tra.
