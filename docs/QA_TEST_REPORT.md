# Biên bản kiểm thử BlueMoon

Biên bản ghi kết quả kiểm thử luồng thu phí và danh sách điểm cần xử lý trước nghiệm thu. Mọi kết quả dưới đây phải gắn với commit, môi trường và bằng chứng cụ thể khi chạy lại.

## Môi trường kiểm thử

| Mục | Giá trị |
|---|---|
| Hệ điều hành | Windows 11 |
| JDK | 25.0.5 |
| MySQL | 8.4 trong container riêng, cổng 3308 |
| Dữ liệu | Schema mới hoàn toàn, sau đó một lượt với schema cũ có ba căn |
| Lệnh chính | `.\mvnw.cmd clean test` với biến `BLUEMOON_IT_DB_*` trỏ container riêng |

Yêu cầu JDK 25 là bắt buộc: chạy bằng JDK 21 dừng ở bước compile với lỗi `release version 25 not supported`, nên không có kết quả test nào trong trường hợp đó.

## Kết quả kiểm thử tự động

Chạy `mvnw clean test` trên MySQL 8.4 tạm riêng, một lần với schema mới hoàn toàn và một lần với schema cũ có ba căn nhưng chưa có user, cư dân hoặc khoản thu. Cả hai lần đều có 14 test đạt, 0 lỗi, 0 bỏ qua.

Ở schema mới, bootstrap tạo bảng nhưng không thêm dữ liệu mẫu; test xác nhận chưa có tài khoản, căn hộ hoặc khoản thu. Tài khoản quản trị đầu tiên được tạo qua service, tổng quan hiển thị kỳ chưa phát hành và số liệu bằng 0. Ở schema cũ, bootstrap giữ nguyên ba căn và không yêu cầu mã căn mẫu còn thiếu; tài khoản quản trị đầu tiên vẫn tạo được.

Chạy JavaFX với một schema trống riêng bằng user MySQL ứng dụng: tiến trình khởi động không có exception và sau đó database vẫn có 0 user, 0 căn hộ, 0 cư dân, 0 khoản thu.

## Kết quả kiểm tra giao diện

Dùng JDK 25.0.5 và MySQL 8.4 trong container riêng ở cổng 3308.

- Chạy `mvnw.cmd "-Dtest=AuthorizationTest,BillingRulesTest,ResidenceChangeServiceTest,CsvExporterTest,FeeNoticePrinterTest" test`: 13 test đạt, 0 lỗi, 0 bỏ qua. Lượt này không chạy lại test tích hợp MySQL vì thay đổi chỉ nằm ở UI và tài liệu.
- Render trực tiếp các view JavaFX tại vùng nội dung 1080 × 700 và 1280 × 820: đăng nhập, tạo quản trị, tổng quan hai vai trò, bảng kê, căn hộ, tài khoản và yêu cầu cư dân. Đã xem ảnh để kiểm tra câu chữ, vị trí menu, bảng rỗng và thanh công cụ xuống dòng.
- Ảnh dùng để kiểm tra nằm trong repo: [đăng nhập](images/dang-nhap.png), [tổng quan](images/tong-quan.png), [bảng kê ở chiều rộng nhỏ](images/bang-ke.png). Bộ render tạm và các ảnh còn lại nằm ở `target/ui-review`, không thuộc mã ứng dụng.
- Schema dùng để render không có user, căn hộ hay khoản thu. Vai trò dùng trong ảnh là đối tượng user riêng của bộ render, không phải kết quả kiểm tra đăng nhập hoặc phân quyền qua UI.

Chạy lại năm lớp trên cùng `DemoDataSqlTest`: 15 test đạt, 0 lỗi, 0 bỏ qua. Hai test trong `DemoDataSqlTest` chỉ kiểm tra nội dung file SQL.

Công cụ truy cập cửa sổ Windows không kết nối được trong lượt này, nên chưa xác minh trực tiếp thao tác chuột/phím, toàn bộ form, dữ liệu dài, lỗi mạng, màn hình khi đang lưu hoặc in thực tế. Ảnh render và các test trên không đủ để đóng R-02 đến R-06 hoặc kết luận toàn bộ UI đạt.

## Kết quả kiểm thử luồng thu phí

Chạy trên schema mới, đối chiếu với ma trận trong [Kế hoạch kiểm thử](QA_TEST_PLAN.md).

| Mã | Kết quả | Ghi chú |
|---|---|---|
| TC-01 | Đạt | Tài khoản khóa sau số lần nhập sai cấu hình |
| TC-02 | Đạt | Trùng mã căn bị từ chối |
| TC-03 | Đạt | Ràng buộc một chủ hộ mỗi căn giữ đúng |
| TC-04 | Đạt | Phát hành trùng tháng bị từ chối |
| TC-05 | Đạt | Số tiền từng căn khớp công thức phí đã chốt |
| TC-06 | Đạt | Bảng kê đã phát hành không đổi khi sửa đơn giá |
| TC-07 | Đạt | Công nợ còn lại đúng sau thanh toán một phần |
| TC-08 | Đạt | Số dư nộp trước cấn đúng vào kỳ sau, không cấn ngược kỳ cũ |
| TC-09 | Đạt | Kế toán bị chặn ở lớp nghiệp vụ khi mở màn hình tài khoản |
| TC-10 | Đạt | In cả kỳ không treo giao diện |

Kiểm thử hồi quy thêm cho hóa đơn tiện ích, phí gửi xe, yêu cầu cư dân và xuất CSV. Không phát hiện lỗi chặn.

## Lỗi đã ghi nhận và xử lý

| Mã | Mức độ | Mô tả | Trạng thái |
|---|---|---|---|
| DEF-01 | Trung bình | Khi chạy Docker, ứng dụng kết nối nhầm cơ sở dữ liệu ngoài localhost làm dữ liệu demo bị lẫn giữa các máy | Đã sửa trong cấu hình kết nối, kiểm tra lại đạt |
| DEF-02 | Cao | Nâng cấp cơ sở dữ liệu đã có dữ liệu lên bản có số dư nộp trước bị lỗi, ứng dụng không khởi động được | Đã sửa, kiểm tra lại đạt |
| DEF-03 | Cao | Tiền nộp dư phát sinh ở kỳ sau bị cấn trừ ngược vào kỳ đã phát hành trước đó, làm sai số đã thanh toán của kỳ cũ | Đã sửa, kiểm tra lại đạt |

## Các điểm còn mở

| Mã | Mức độ | Ghi nhận từ source | Trạng thái thực thi | Case kiểm tra lại |
|---|---|---|---|---|
| R-02 | Cao | Chưa có khóa chống ghi lặp cùng một yêu cầu thu tiền; mã giao dịch chưa unique. | Chưa chạy gửi lặp/đồng thời. | PAY-04, PAY-05 |
| R-03 | Cao | Chọn giá theo ngày phát hành, khác yêu cầu đầu kỳ; test tích hợp đang mong đợi theo hành vi hiện tại. | Chưa chạy case giá giữa tháng. | RATE-01, RATE-02 |
| R-04 | Trung bình | Chứng từ/lịch sử join mã căn và tên hộ hiện tại. | Cần chốt chính sách; chưa chạy đổi hồ sơ/in lại. | HIST-01 |
| R-05 | Trung bình | Tên liên hệ căn hộ và cư dân HEAD có thể không nhất quán. | Cần chốt mô hình/nhãn; chưa chạy lại trên UI. | RES-04 |
| R-06 | Trung bình | Nhiều handler JDBC còn đồng bộ trên luồng giao diện. | Chưa đo phản hồi khi DB chậm. | UI-01, UI-02 |

Chi tiết hướng xử lý: [Đối chiếu Sprint 1](SPRINT1_COVERAGE_REVIEW.md).

## Phạm vi các test tự động

| Lớp | Số phương thức `@Test` | Nội dung chính và giới hạn |
|---|---:|---|
| `AuthorizationTest` | 3 | Chặn accountant sửa căn/phát hành/quản lý tiện ích, xe, yêu cầu; chặn admin thu tiền. Chưa có kiểm tra `AccountService` trong lớp này. |
| `BillingRulesTest` | 5 | Công thức, trạng thái và validation tiền. Chưa kiểm tra lựa chọn ngày đơn giá ở SQL. |
| `ResidenceChangeServiceTest` | 3 | Validation ngày và lý do; chưa xác minh lưu MySQL. |
| `CsvExporterTest` | 1 | UTF-8/BOM, header, escape và chặn tiền tố công thức trong tệp. Chưa kiểm tra UI chọn tệp/ghi đè. |
| `FeeNoticePrinterTest` | 1 | Chuỗi nội dung chuyển khoản. Chưa kiểm tra bố cục, máy in/PDF hoặc UI không treo. |
| `MySqlBillingFlowTest` | 1 | Một luồng tích hợp gồm nhiều assertion: hồ sơ, tài khoản, phí, mở rộng, snapshot, migration thanh toán, công nợ và credit. Skip nếu thiếu URL test; có thay schema và xóa fixture. |
| `DemoDataSqlTest` | 2 | Đọc file SQL để kiểm tra các mã căn, quan hệ và nhóm bản ghi; chưa xác minh thực thi SQL hoặc dữ liệu sau khi nạp. |
| Tổng trong source | 16 | 15 test không cần MySQL và 1 test tích hợp. Số test khai báo không thay thế kết quả chạy. |

Các mã case trong [Kế hoạch kiểm thử](QA_TEST_PLAN.md) không ánh xạ một-một sang JUnit. Một test tích hợp có nhiều assertion không đồng nghĩa toàn bộ ma trận QA, nhất là migration legacy, chống thu trùng và in thực tế, đã được bao phủ.

## Hồ sơ cần ghi cho mỗi lần chạy

| Trường | Nội dung phải ghi |
|---|---|
| Bản dựng | Commit SHA chính xác, nhánh và trạng thái có thay đổi chưa commit hay không. |
| Người chạy, thời gian | Tên người chạy, ngày giờ và múi giờ. |
| Môi trường | Phiên bản OS, JDK, Maven, MySQL; cấu hình máy in/PDF nếu kiểm tra in. |
| Dữ liệu | Tên schema kiểm thử, bộ A/B/C/D/E, bước khởi tạo và trạng thái trước/sau. |
| Kết quả tự động | Lệnh chạy, tổng test, đạt/lỗi/bỏ qua và bản sao `target/surefire-reports/`. |
| Kết quả thủ công | Mã case, actual/expected, ảnh/PDF/CSV/SQL đối chiếu và mã lỗi nếu có. |
| Quyết định nghiệm thu | Các mục đã sửa, case đã chạy lại và quyết định cho mục còn mở. |

## Kết luận

Luồng thu phí tháng 10/2026 đạt Definition of Done của Sprint 1 sau khi xử lý DEF-01, DEF-02 và DEF-03. Các mục R-02 đến R-06 vẫn cần quyết định của PM trước khi nghiệm thu chính thức.
