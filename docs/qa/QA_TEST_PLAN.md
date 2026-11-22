# Kế hoạch kiểm thử

## Mục tiêu

Kiểm tra BlueMoon trước khi bàn giao, tập trung vào việc ứng dụng có làm đúng những gì ban quản trị và kế toán cần làm hằng tháng không: lập khoản phải thu, phát hành kỳ thu, thu tiền của cư dân, theo dõi công nợ.

Hai câu hỏi cần trả lời: nghiệp vụ thu phí có chạy đúng từ đầu đến cuối không, và người dùng có thao tác được trên giao diện thật không.

## Phạm vi

**Có kiểm tra:**

- Đăng nhập và phân quyền giữa ban quản trị với kế toán.
- Quản lý căn hộ, nhân khẩu và chủ hộ.
- Thiết lập khoản thu và đơn giá có ngày hiệu lực.
- Phát hành kỳ thu theo tháng và sinh bảng kê.
- Xem trước và in thông báo thu phí, xuất CSV từ dữ liệu đang hiển thị.
- Ghi nhận thanh toán, xử lý nộp dư và theo dõi công nợ.
- Xem tổng quan và lịch sử giao dịch.

**Không kiểm tra trong đợt nghiệm thu này:** phương tiện và phí gửi xe, hóa đơn điện nước internet, yêu cầu cư dân, đóng góp tự nguyện, biến động cư trú. Các phần này có trong mã nguồn, nên chỉ chạy hồi quy để chắc chúng không làm hỏng luồng thu phí.

## Cách làm

Kiểm tra theo bốn lớp, từ thấp lên cao:

1. **Đơn vị:** công thức tính tiền và quy tắc dữ liệu, chạy tự động bằng JUnit, không cần database.
2. **Tích hợp:** chạy luồng nghiệp vụ trên database demo MySQL của project, có tạo và xóa bản ghi tạm của case.
3. **Giao diện:** mở ứng dụng thật, thao tác bằng chuột và bàn phím, chụp ảnh màn hình.
4. **Đầu cuối:** chạy một kịch bản liền mạch từ tạo căn hộ đến xem công nợ, không bỏ bước.

Mỗi lớp có tài liệu riêng: [kịch bản kiểm thử](QA_TEST_SCENARIO.md), [test case chức năng](QA_TEST_CASE_FUNCTIONAL.md), [test case giao diện](QA_TEST_CASE_UI.md), [test case tích hợp](QA_TEST_CASE_INTEGRATION.md) và [dữ liệu kiểm thử](QA_TEST_DATA.md).

## Môi trường

| Thành phần | Yêu cầu |
|---|---|
| Máy chạy giao diện | Windows 10 hoặc 11, màn hình 1920x1080, có máy in PDF |
| Java | JDK 25, kiểm tra bằng `.\mvnw.cmd -version` |
| Maven | Maven Wrapper 3.9.16 kèm trong dự án |
| Cơ sở dữ liệu | MySQL 8.4 chạy bằng Docker Compose, database `bluemoon`, cổng 3307 |
| Dữ liệu mẫu | `src/main/resources/db/schema.sql` nạp trước, sau đó `database/demo-data.sql` nạp dữ liệu demo |
| Tài khoản thử | Ba tài khoản mẫu trong [dữ liệu kiểm thử](QA_TEST_DATA.md) |

Database QA duy nhất là `bluemoon` do Docker Compose của project quản lý ở cổng `3307`. Mọi test MySQL trong kế hoạch chạy trên database này; không dựng thêm database, schema hay container riêng. Ghi lại phiên bản từ `.\mvnw.cmd -version`, `docker version` và commit từ `git rev-parse HEAD` để người khác chạy lại được.

Dữ liệu đầu vào không đúng điều kiện của case thì dừng lại và ghi Bị chặn.

## Lịch

| Tuần | Nội dung | Kết quả cần có |
|---|---|---|
| Tuần 1 | Chốt phạm vi, viết kịch bản và test case | PM duyệt bộ tài liệu QA |
| Tuần 2 | Chạy test tự động, sửa lỗi chặn | Báo cáo test tự động không còn lỗi chặn |
| Tuần 3 | Kiểm tra giao diện và in ấn | Ảnh màn hình cho từng màn hình chính |
| Tuần 4 | Chạy kịch bản đầu cuối, tổng hợp | Biên bản kiểm thử và danh sách lỗi còn mở |

## Phân công

| Vai trò | Người phụ trách | Việc chính |
|---|---|---|
| Kiểm thử | thunganle272 | Viết test case, chạy kiểm tra, ghi biên bản |
| Phát triển | minhduong06-td, Danh Quan, HaTienDat2004 | Sửa lỗi ghi trong biên bản |
| Duyệt | PhamVanAn268 | Duyệt phạm vi, quyết định lỗi nào phải sửa trước nghiệm thu |

## Tiêu chí dừng

Chuyển sang nghiệm thu khi thỏa cả bốn điều:

1. Không còn lỗi mức cao chưa xử lý.
2. Mọi test case ưu tiên 1 đã chạy và đạt.
3. Kịch bản đầu cuối chạy hết một lượt, không lỗi chặn.
4. Biên bản có đủ ảnh, log và số liệu đối chiếu.

Còn lỗi mức trung bình thì PM quyết định chấp nhận và ghi lý do vào biên bản.

## Rủi ro

Cần hồi quy trước nghiệm thu: đăng nhập và phân quyền; hồ sơ và chủ hộ; phát hành kỳ và chống trùng; đơn giá giữa tháng (FUN-29); thanh toán, cấn trừ và gửi trùng/đồng thời (FUN-24, FUN-30); in chứng từ và thông tin lịch sử. Các tiêu chí Sprint 2 (US-028 đến US-031) chưa có test case trong repo nên chưa đưa vào đợt này. Ghi kết quả từng case vào biên bản; case chưa chạy không tính là đạt.

| Rủi ro | Ảnh hưởng | Cách giảm |
|---|---|---|
| Máy kiểm tra thiếu JDK 25 | Không biên dịch được, không chạy được test | Kiểm tra `mvnw -version` trước mỗi lượt |
| Trỏ nhầm database khi chạy test có ghi dữ liệu | Mất dữ liệu demo, kết quả sai | Kiểm tra URL trước mỗi lượt, chỉ trỏ tới `bluemoon` ở cổng 3307 |
| Không có máy in thật | Không kiểm tra được bản in giấy | Dùng máy in PDF, lưu tệp làm bằng chứng |
| Dữ liệu thử lẫn với dữ liệu thật | Số liệu đối chiếu không khớp | Dùng bộ dữ liệu riêng trong [dữ liệu kiểm thử](QA_TEST_DATA.md) |
| Cuối kỳ gấp thời gian | Bỏ sót case giao diện | Ưu tiên case mức 1 và kịch bản đầu cuối trước |

## Tài liệu tham chiếu

- [Kịch bản kiểm thử](QA_TEST_SCENARIO.md)
- [Test case chức năng](QA_TEST_CASE_FUNCTIONAL.md)
- [Test case giao diện](QA_TEST_CASE_UI.md)
- [Test case tích hợp](QA_TEST_CASE_INTEGRATION.md)
- [Dữ liệu kiểm thử](QA_TEST_DATA.md)
- [Biên bản kiểm thử](QA_TEST_REPORT.md)
- [Quy tắc nghiệp vụ](../guides/NGHIEP_VU.md)
