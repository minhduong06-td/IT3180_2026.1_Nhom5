# Giải trình và hướng dẫn cho QA

## QA dựa vào đâu để cho đạt?

Một case đạt khi kết quả thực tế khớp expected đã xác định từ yêu cầu, có bằng chứng và môi trường tái hiện được. Không cho đạt chỉ vì không thấy lỗi trên màn hình, code có test hoặc Maven báo build thành công.

Với mỗi case, ghi: commit, môi trường, trạng thái dữ liệu ban đầu, bước làm, expected, actual, kết luận và bằng chứng. Dùng Đạt, Không đạt, Bị chặn hoặc Chưa chạy. Test bị bỏ qua không được tính là Đạt.

## Chuẩn bị môi trường

Cần JDK 25, Docker Desktop và một database riêng. Không kiểm tra phá dữ liệu trên database đang sử dụng. Ghi phiên bản từ `.\mvnw.cmd -version`, `docker version` và commit từ `git rev-parse HEAD`.

Kiểm tra không cần MySQL:

```powershell
.\mvnw.cmd "-Dtest=AuthorizationTest,BillingRulesTest,ResidenceChangeServiceTest,CsvExporterTest,FeeNoticePrinterTest,DemoDataSqlTest" test
```

Nhóm này có 15 phương thức test: quyền, công thức tiền, validation cư trú, CSV, nội dung chuyển khoản và hai kiểm tra nội dung file SQL khởi tạo. Hai kiểm tra SQL chỉ đọc file, không xác minh việc thực thi trên MySQL.

### Chạy tích hợp với MySQL rỗng

Tạo container riêng tại cổng 3308. Tên container dưới đây phải chưa tồn tại.

```powershell
docker run --name bluemoon-qa-mysql -d -p 127.0.0.1:3308:3306 -e MYSQL_DATABASE=bluemoon_qa -e MYSQL_USER=bluemoon_qa -e MYSQL_PASSWORD=bluemoon_qa_dev -e MYSQL_ROOT_PASSWORD=bluemoon_qa_root mysql:8.4
```

Chờ MySQL sẵn sàng; chạy lại truy vấn sau cho tới khi trả về 1:

```powershell
docker exec bluemoon-qa-mysql mysql -h 127.0.0.1 -u bluemoon_qa -pbluemoon_qa_dev bluemoon_qa -e "SELECT 1;"
```

Trong cùng cửa sổ PowerShell:

```powershell
$env:BLUEMOON_IT_DB_URL = 'jdbc:mysql://localhost:3308/bluemoon_qa?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_IT_DB_USER = 'bluemoon_qa'
$env:BLUEMOON_IT_DB_PASSWORD = 'bluemoon_qa_dev'
Remove-Item Env:BLUEMOON_IT_DB_LEGACY -ErrorAction SilentlyContinue
.\mvnw.cmd clean test
```

Mong đợi 16 test chạy, 0 lỗi, 0 bỏ qua. Sao chép `target/surefire-reports` trước lần chạy tiếp theo. Test tích hợp tạo fixture, thay schema và dọn một phần dữ liệu; mỗi lượt phải dùng schema mới hoàn toàn.

Để chạy nhánh legacy, tạo container khác với database rỗng theo cách trên, dùng cổng khác và đổi URL tương ứng; đặt `$env:BLUEMOON_IT_DB_LEGACY = 'true'` rồi chạy test. Nhánh này tạo schema căn hộ cũ có ba căn và kiểm tra nâng cấp, không chạy toàn bộ phần billing của test tích hợp.

Sau khi giữ bằng chứng, xóa container riêng và volume của nó bằng `docker rm -f -v bluemoon-qa-mysql`. Xóa biến `BLUEMOON_IT_DB_*` hoặc đóng cửa sổ PowerShell khi xong.

## Kiểm tra thủ công từ database rỗng

Dùng một database riêng khác với database vừa chạy integration test. Đặt `BLUEMOON_DB_*` trỏ tới đó trước khi mở app; biến này độc lập với `BLUEMOON_IT_DB_*`.

| Bước | Thao tác | Expected và bằng chứng |
|---|---|---|
| Khởi tạo | Mở app với schema chưa có bảng | Màn hình tạo quản trị, không có user/căn/phí tự thêm. Kiểm tra bảng và ảnh màn hình. |
| Tạo quản trị | Thử username sai, mật khẩu ngắn, xác nhận lệch; sau đó nhập hợp lệ | Sai thì không tạo user; đúng thì có một ADMIN và mở app. Đóng/mở lại phải vào đăng nhập. |
| Hồ sơ | Tạo A101, mã hộ riêng, diện tích 48 m², ngày nhận nhà hợp lệ; thêm chủ hộ hợp lệ | Lưu được, mở lại không mất. Mã căn/mã hộ/giấy tờ trùng bị chặn; không tạo thêm dòng. |
| Khoản thu | Tạo dịch vụ 10.000 đồng/m² và quản lý 7.000 đồng/m², đang áp dụng, giá hiệu lực từ đầu kỳ | Danh mục và lịch sử giá đúng. Giá không dương hoặc trùng ngày hiệu lực bị chặn. |
| Kỳ thu | Chọn một tháng, ngày phát hành ngày 01, hạn nộp sau đó | Hai charge cho căn, lần lượt 480.000 và 336.000 đồng, tổng 816.000. Phát hành lại cùng tháng bị chặn. |
| Quyền | Tạo kế toán, đăng nhập kế toán; kiểm tra cả UI và service | Kế toán không sửa hồ sơ/phát hành kỳ. ADMIN không được thu tiền khoản phải thu. |
| Thu một phần | Kế toán nhận 200.000 cho dịch vụ | Phiếu 200.000, khoản dịch vụ còn 280.000, tổng nợ hộ 616.000. |
| Thu dư | Nhận tiếp 300.000 cho dịch vụ | Cấn 280.000, dư 20.000, dịch vụ đã đủ, quản lý còn 336.000. |
| Kỳ sau | Giữ nguyên giá, ADMIN phát hành tháng sau | Tổng khoản mới 816.000, cấn dư 20.000, còn nợ kỳ mới 796.000, số dư còn 0. Không gộp nợ quản lý kỳ cũ vào con số này. |
| Đối soát | Mở bảng kê, công nợ, tổng quan và lịch sử thu | Tổng phiếu nhận 500.000; cấn từ hai phiếu 480.000; cấn kỳ sau 20.000. Kiểm tra theo từng kỳ và hộ. |
| In/CSV | Xem thông báo, in hoặc xuất PDF, xuất CSV | Đúng hộ, khoản, kỳ, tiền và thông tin chuyển khoản; chữ không cắt, CSV đủ dòng đang lọc và giữ tiếng Việt. |
| UI | Tab/Enter, lỗi nhập, bảng rỗng, dữ liệu dài, cửa sổ 1080 × 700 và 1280 × 820 | Nhãn đọc được, focus rõ, menu cuộn tới cuối, không mất nút. Nội dung hướng dẫn không lặp tiêu đề. |

Case nhận tiền phải kiểm tra số dòng trong `payments`, giá trị `applied_amount`, số dư và các charge liên quan. Ảnh toast thành công không đủ chứng minh số liệu đúng.

Phương tiện, tiện ích, yêu cầu cư dân, đóng góp và cư trú có case riêng trong [ma trận QA](QA_TEST_PLAN.md). Hóa đơn tiện ích phải bị chặn sửa/xóa sau khi đã phát hành; đóng góp không tạo công nợ; biển số đang hoạt động không được trùng.

## 14 test đạt có nghĩa là gì?

[Biên bản đã ghi nhận](QA_TEST_REPORT.md) có các lượt JDK 25 + MySQL 8.4 với 14 test đạt trên schema mới và legacy. Có 13 test không cần MySQL và một test tích hợp gồm nhiều assertion. Hai chế độ chạy nhánh khác nhau; 14 test đạt ở legacy không có nghĩa toàn bộ luồng billing đã chạy lần nữa.

Lượt JDK 21 trước đó dừng khi compile nên không có kết quả test. Lượt khởi động JavaFX với database rỗng chỉ xác nhận khởi động không exception, chưa chứng minh mọi thao tác UI hoặc in đạt.

## Những trường hợp không được cho qua

| Case | Kết luận hiện tại |
|---|---|
| Gửi lại cùng yêu cầu thu một phần hoặc gửi đồng thời | R-02 còn mở. Nếu tạo hai phiếu thì Không đạt; cần kiểm tra cả DB. |
| Giá tăng giữa tháng, phát hành sau ngày tăng | R-03 còn mở. Theo yêu cầu vẫn dùng giá ngày đầu kỳ, không lấy giá ngày phát hành làm expected. |
| Đổi mã căn/tên hộ rồi in lại chứng từ cũ | R-04 còn mở; kiểm tra sự thay đổi và đối chiếu chính sách giữ lịch sử. |
| Sửa tên liên hệ khác với chủ hộ | R-05 còn mở; không tự kết luận hai trường là cùng ý nghĩa. |
| MySQL chậm, nhấn thao tác nhiều lần, in cả kỳ | R-06 chưa có kết quả đủ để kết luận; cần kiểm tra phản hồi và dữ liệu ghi lặp. |

QA có thể cho đạt từng case đã có bằng chứng. Kết luận toàn bộ bản dựng cần đối chiếu yêu cầu và lỗi chặn còn mở với PM.

## Trả lời khi được hỏi “vì sao cho pass?”

Nêu mã case, dữ liệu đầu vào, expected tính độc lập, actual đối chiếu và nơi lưu bằng chứng. Ví dụ: “Căn 48 m² × 10.000 đồng = 480.000 đồng; bảng kê và DB đều ghi 480.000 đồng, chỉ có một charge cho căn/phí/kỳ. Case tính phí này đạt. Case giá giữa tháng được theo dõi riêng và chưa đạt.”

Không đưa ví dụ này vào báo cáo như kết quả đã chạy nếu chưa thực hiện trên bản dựng đang kiểm tra.
