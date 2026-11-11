# Biên bản kiểm thử BlueMoon

Ngày cập nhật tài liệu: 11/11/2026. Mã nguồn tham chiếu: `7d43b3f1526de62036da1056477019e81c5299ba`.

Biên bản tách kết quả kiểm tra hiện tại khỏi lịch sử các lần chạy đã ghi trong repo. Danh sách test trong source và các phép tính expected không được tính là kết quả đã thực thi.

## Trạng thái xác minh ngày 11/11/2026

| Mục | Ghi nhận |
|---|---|
| Cách kiểm tra | Đọc code Java, schema, test, README và 4 file docs; thử chạy Maven test. |
| Java thực tế | Oracle Java 21.0.10; source yêu cầu JDK 25. |
| Docker/MySQL | Lệnh `docker` không có trong môi trường kiểm tra; chưa chạy được MySQL tích hợp. |
| Lệnh đã thử | `.\mvnw.cmd test -q` tại thư mục repo. |
| Kết quả build | Dừng ở bước compile: `error: release version 25 not supported`. |
| Test tự động của lượt này | Chưa chạy đến test do compile bị chặn; không có kết quả đạt cho 14 test. |
| UI, in và SQL thực tế | Chưa thực hiện trong lượt này. |
| Phạm vi thay đổi tài liệu | README và docs; không sửa code Java, schema hoặc test để xử lý R-01–R-06. |

Trích lỗi biên dịch:

```text
Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile
Fatal error compiling: error: release version 25 not supported
```

Đây là lỗi môi trường Java không đúng yêu cầu dự án. Cần cấu hình JDK 25 theo [README](../README.md) rồi chạy lại; chưa có cơ sở kết luận các test nghiệp vụ đạt hoặc không đạt từ lần build này.

## Các vấn đề cần xác minh sau khi sửa

| Mã | Mức độ | Ghi nhận từ source | Trạng thái thực thi hiện tại | Case kiểm tra lại |
|---|---|---|---|---|
| R-01 | Cao | Seed cư dân phụ thuộc đủ mã căn demo; fixture DB cũ có thể lỗi ở A104. | Chưa sửa code; chưa tái hiện MySQL trong lượt 11/11. | MIG-01, MIG-02 |
| R-02 | Cao | Chưa có khóa chống ghi lặp cùng một yêu cầu thu tiền; mã giao dịch chưa unique. | Chưa sửa code; chưa chạy gửi lặp/đồng thời. | PAY-04, PAY-05 |
| R-03 | Cao | Chọn giá theo ngày phát hành, khác yêu cầu đầu kỳ; test tích hợp đang mong đợi theo hành vi hiện tại. | Chưa sửa code/test; chưa chạy case giá giữa tháng. | RATE-01, RATE-02 |
| R-04 | Trung bình | Chứng từ/lịch sử join mã căn và tên hộ hiện tại. | Cần chốt chính sách và sửa; chưa chạy đổi hồ sơ/in lại. | HIST-01 |
| R-05 | Trung bình | Tên liên hệ căn hộ và cư dân HEAD có thể không nhất quán. | Cần chốt mô hình/nhãn; chưa chạy lại trên UI. | RES-04 |
| R-06 | Trung bình | Nhiều handler JDBC còn đồng bộ trên luồng giao diện. | Chưa sửa code; chưa đo phản hồi khi DB chậm. | UI-01, UI-02 |

Chi tiết hướng sửa: [Đối chiếu Sprint 1](SPRINT1_COVERAGE_REVIEW.md). Mã R dùng cho các điểm còn mở trong bản tài liệu này, tách với DEF-01/DEF-02 đã ghi ở lịch sử.

## Phạm vi các test tự động đang có

| Lớp | Số phương thức `@Test` | Nội dung chính và giới hạn |
|---|---:|---|
| `AuthorizationTest` | 3 | Chặn accountant sửa căn/phát hành/quản lý tiện ích, xe, yêu cầu; chặn admin thu tiền. Chưa có kiểm tra `AccountService` trong lớp này. |
| `BillingRulesTest` | 5 | Công thức, trạng thái và validation tiền. Chưa kiểm tra lựa chọn ngày đơn giá ở SQL. |
| `ResidenceChangeServiceTest` | 3 | Validation ngày và lý do; chưa xác minh lưu MySQL. |
| `CsvExporterTest` | 1 | UTF-8/BOM, header, escape và chặn tiền tố công thức trong tệp. Chưa kiểm tra UI chọn tệp/ghi đè. |
| `FeeNoticePrinterTest` | 1 | Chuỗi nội dung chuyển khoản. Chưa kiểm tra bố cục, máy in/PDF hoặc UI không treo. |
| `MySqlBillingFlowTest` | 1 | Một luồng tích hợp gồm nhiều assertion: hồ sơ, tài khoản, phí, mở rộng, snapshot, migration thanh toán, công nợ và credit. Skip nếu thiếu URL test; có thay schema và xóa fixture. |
| Tổng trong source | 14 | 13 test không cần MySQL và 1 test tích hợp; đây là số lượng khai báo, không phải số đạt của lượt 11/11. |

Các mã case trong [Kế hoạch kiểm thử](QA_TEST_PLAN.md) chưa ánh xạ một-một sang JUnit. Một test tích hợp có nhiều assertion không đồng nghĩa toàn bộ ma trận QA, nhất là migration legacy, chống thu trùng và in thực tế, đã được bao phủ.

## Hồ sơ cần bổ sung cho lần chạy tiếp theo

| Trường | Nội dung phải ghi khi chạy |
|---|---|
| Bản dựng | Commit SHA chính xác, nhánh và trạng thái có thay đổi chưa commit hay không. |
| Người chạy, thời gian | Tên người chạy, ngày giờ và múi giờ. |
| Môi trường | Phiên bản OS, JDK, Maven, MySQL; cấu hình máy in/PDF nếu kiểm tra in. |
| Dữ liệu | Tên schema kiểm thử, bộ A/B/C/D/E, bước khởi tạo và trạng thái trước/sau. |
| Kết quả tự động | Lệnh chạy, tổng test, đạt/lỗi/bỏ qua và bản sao `target/surefire-reports/`. |
| Kết quả thủ công | Mã case, actual/expected, ảnh/PDF/CSV/SQL đối chiếu và mã lỗi nếu có. |
| Quyết định nghiệm thu | Các mục đã sửa, case đã chạy lại và quyết định cho mục còn mở. |

Chưa chốt lại Definition of Done cho bản mã tham chiếu chỉ dựa trên các bản ghi cũ dưới đây. Cần khắc phục môi trường, xử lý các điểm ưu tiên cao và lưu bằng chứng kiểm tra lại.

## Lịch sử được giữ từ tài liệu trước

Phần dưới giữ lại nội dung các lần chạy mà tài liệu trước đã ghi, gồm cả kết luận tại thời điểm đó. Repo chưa có commit SHA cụ thể và log/ảnh kèm theo để xác minh lại từng lần. Các trạng thái “Đạt”, “Đã sửa” và tổng 14 test đạt là **ghi nhận lịch sử**, không được cộng vào kết quả ngày 11/11/2026 hoặc dùng để đóng R-01–R-06.

Ghi nhận kết quả chạy kiểm thử luồng thu phí trên cơ sở dữ liệu sạch, đối chiếu với `QA_TEST_PLAN.md`.

### Lần chạy 1 - 30/10/2026

Môi trường: Windows 11, Java 25, MySQL 8.4 qua Docker, cơ sở dữ liệu khởi tạo lại từ đầu.
Bản dựng: main sau merge các nhánh tính đến `feature/debt-overview`.

| Mã | Kết quả | Ghi chú |
|---|---|---|
| TC-01 | Đạt | Tài khoản khóa sau số lần nhập sai cấu hình |
| TC-02 | Đạt | Trùng mã căn bị từ chối |
| TC-03 | Đạt | Ràng buộc một chủ hộ mỗi căn giữ đúng |
| TC-04 | Đạt | Phát hành trùng tháng bị từ chối |
| TC-05 | Đạt | Đúng với phạm vi đã có: diện tích, nhân khẩu và phí cố định; chưa có phần phương tiện |
| TC-06 | Đạt | Bảng kê đã phát hành không đổi khi sửa đơn giá |
| TC-07 | Đạt | Công nợ còn lại đúng sau thanh toán một phần |
| TC-08 | Chưa chạy | Số dư nộp trước chưa có trong bản dựng 30/10 |
| TC-09 | Đạt | Kế toán bị chặn ở lớp nghiệp vụ khi mở màn hình tài khoản |
| TC-10 | Đạt | In cả kỳ không treo giao diện |

Tổng: 9 đạt, 0 không đạt, 1 chưa chạy.

Danh sách lỗi lần chạy 1:

| Mã | Mức độ | Mô tả | Trạng thái |
|---|---|---|---|
| DEF-01 | Trung bình | Khi chạy Docker, ứng dụng kết nối nhầm cơ sở dữ liệu ngoài localhost làm dữ liệu demo bị lẫn giữa các máy | Đã sửa trong cấu hình kết nối, kiểm tra lại đạt |

### Lần chạy 2 - 07/11/2026

Môi trường: như lần chạy 1, cơ sở dữ liệu khởi tạo lại từ đầu sau khi merge toàn bộ nhánh tính đến `fix/payment-migration-and-coverage`.
Phạm vi: chạy đủ 10 test case, gồm số dư nộp trước và kiểm thử hồi quy các story sau Sprint 1.

| Mã | Kết quả | Ghi chú |
|---|---|---|
| TC-01 đến TC-07 | Đạt | Không hồi quy so với lần chạy 1 |
| TC-08 | Không đạt | DEF-02 |
| TC-09 | Đạt | Không hồi quy |
| TC-10 | Đạt | Không hồi quy |

Tổng lần chạy 2: 9 đạt, 1 không đạt.

Danh sách lỗi lần chạy 2:

| Mã | Mức độ | Mô tả | Trạng thái |
|---|---|---|---|
| DEF-02 | Cao | Nâng cấp cơ sở dữ liệu đã có dữ liệu từ bản trước lên bản có số dư nộp trước bị lỗi, ứng dụng không khởi động được | Đã sửa, kiểm tra lại đạt |

DEF-02 tương ứng nhánh `fix/payment-migration-and-coverage`.

Lỗi cấn trừ số dư vào kỳ cũ đã được phát hiện và sửa trong lúc dev chạy kiểm thử tích hợp ở nhánh `feature/payment-receipts`, trước khi QA nhận bản dựng, nên không ghi thành lỗi riêng ở đây.

### Lần chạy 3 - 08/11/2026

Chạy lại sau khi merge nhánh sửa lỗi, cơ sở dữ liệu sạch và thêm một lượt nâng cấp từ bản cũ.

| Mã | Kết quả | Ghi chú |
|---|---|---|
| TC-01 đến TC-10 | Đạt | Không còn lỗi chặn |

Kiểm thử hồi quy thêm cho hóa đơn tiện ích, phí gửi xe, yêu cầu cư dân và xuất CSV. Không phát hiện lỗi chặn.

### Kết luận được ghi ở bản trước

Luồng thu phí tháng 10/2026 đạt Definition of Done của Sprint 1 sau khi sửa DEF-01 và DEF-02. Đủ điều kiện chạy demo nội bộ.

### Bản ghi kiểm thử ngày 10/11/2026

Lần chạy này được thực hiện lại sau khi bổ sung dữ liệu demo và cập nhật test tích hợp cho fixture mới. Môi trường dùng JDK 25, MySQL 8.4 từ `compose.yaml`, database sạch và chạy với các biến `BLUEMOON_IT_DB_*`.

| Nhóm | Kết quả |
|---|---:|
| `MySqlBillingFlowTest` | 1 đạt |
| `AuthorizationTest` | 3 đạt |
| `BillingRulesTest` | 5 đạt |
| `ResidenceChangeServiceTest` | 3 đạt |
| `CsvExporterTest` | 1 đạt |
| `FeeNoticePrinterTest` | 1 đạt |
| Tổng | 14 đạt, 0 lỗi, 0 bỏ qua |

Database sau bootstrap được kiểm tra trực tiếp có 8 căn hộ, 18 nhân khẩu, 0 kỳ thu và 0 giao dịch thanh toán. Việc chưa có kỳ thu/giao dịch sau bootstrap là có chủ ý: demo phải tạo chúng qua giao diện để chứng minh luồng nghiệp vụ, thay vì chỉ mở dữ liệu đã chèn sẵn.
