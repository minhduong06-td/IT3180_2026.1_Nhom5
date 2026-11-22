# Hướng dẫn kiểm thử

## Tiêu chí đạt

Một case đạt khi kết quả thực tế khớp expected xác định từ yêu cầu, có bằng chứng và môi trường tái hiện được. Không cho đạt chỉ vì màn hình không báo lỗi, code có test hay Maven build xanh.

Mỗi case ghi: commit, môi trường, trạng thái dữ liệu ban đầu, các bước, expected, actual, kết luận, nơi lưu bằng chứng. Kết luận dùng một trong bốn: Đạt, Không đạt, Bị chặn, Chưa chạy. Test bị bỏ qua ghi là Chưa chạy.

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

Lệnh trên chỉ chạy các lớp được chọn. `DemoDataSqlTest` đọc nội dung file SQL, không chạy SQL đó trên MySQL. Số test thực tế xem trong báo cáo Surefire của từng lượt.

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

Case nhận tiền phải đối chiếu số dòng trong `payments`, giá trị `applied_amount`, số dư và các charge liên quan. Ảnh toast thành công không đủ để kết luận số liệu đúng.

Phương tiện, tiện ích, yêu cầu cư dân, đóng góp và cư trú có case riêng trong [test case chức năng](QA_TEST_CASE_FUNCTIONAL.md). Hóa đơn tiện ích bị chặn sửa/xóa sau khi phát hành; đóng góp không tạo công nợ; biển số đang hoạt động không trùng nhau.

## Cách đọc kết quả

[Biên bản kiểm thử](QA_TEST_REPORT.md) có thể còn ghi các lượt chạy lịch sử trên schema riêng. Những số liệu đó không phải kết quả của quy trình hiện tại. Kết quả được chấp nhận hiện tại phải ghi rõ đã chạy trên database demo `bluemoon` và được tạo từ `schema.sql` cùng `demo-data.sql`.

Lượt JDK 21 trước đó dừng ở bước compile nên không có kết quả test. Lượt mở JavaFX với database rỗng chỉ cho biết app khởi động không exception.

## Case chưa được kết luận đạt

| Case | Tình trạng |
|---|---|
| Gửi lại cùng yêu cầu thu một phần, hoặc gửi đồng thời | Dùng cùng mã giao dịch; phải chỉ có một phiếu trong DB. Case MySQL ngày 25/11 đạt. Hai mã khác nhau cho cùng giao dịch thì chưa nhận diện được. |
| Giá tăng giữa tháng, phát hành sau ngày tăng | Theo yêu cầu phải dùng giá ngày đầu kỳ. Case MySQL ngày 25/11 đạt, cần chạy lại khi sửa chức năng tính phí. |
| Đổi mã căn/tên hộ rồi in lại chứng từ cũ | R-04 còn mở; kiểm tra mức thay đổi rồi đối chiếu chính sách giữ lịch sử. |
| Sửa tên liên hệ khác tên chủ hộ | R-05 còn mở; chưa kết luận hai trường cùng ý nghĩa. |
| MySQL chậm, bấm thao tác nhiều lần, in cả kỳ | R-06 chưa đủ kết quả; cần kiểm tra độ phản hồi và dữ liệu ghi lặp. |

Từng case đã có bằng chứng thì QA cho đạt được. Kết luận cho cả bản dựng thì phải đối chiếu yêu cầu và các lỗi chặn còn mở với PM.

Khi bị hỏi vì sao cho đạt một case, trả lời bằng mã case, dữ liệu đầu vào, expected tính độc lập, actual đối chiếu và nơi lưu bằng chứng. Ví dụ: "Căn 48 m² × 10.000 đồng = 480.000 đồng; bảng kê và DB đều ghi 480.000 đồng, chỉ có một charge cho căn/phí/kỳ. Case tính phí đạt. Case giá giữa tháng theo dõi riêng, chưa đạt." Ví dụ này chỉ dùng khi đã chạy thật trên bản dựng đang kiểm tra.
