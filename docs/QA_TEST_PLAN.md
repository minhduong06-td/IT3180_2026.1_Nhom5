# Kế hoạch kiểm thử

## 1. Mục tiêu

Kế hoạch này mô tả cách nhóm kiểm tra BlueMoon trước khi bàn giao cho khách hàng. Mục tiêu là xác nhận ứng dụng làm đúng việc ban quản trị và kế toán cần làm hằng tháng: lập khoản phải thu, phát hành kỳ thu, thu tiền của cư dân, và theo dõi công nợ.

Kế hoạch không nhằm chứng minh ứng dụng hoàn hảo. Kế hoạch nhằm trả lời hai câu hỏi: nghiệp vụ thu phí có chạy đúng từ đầu đến cuối không, và người dùng có thao tác được trên giao diện thật không.

## 2. Phạm vi

### Trong phạm vi

- Đăng nhập và phân quyền giữa ban quản trị với kế toán.
- Quản lý căn hộ, nhân khẩu và chủ hộ.
- Thiết lập khoản thu và đơn giá có ngày hiệu lực.
- Phát hành kỳ thu theo tháng và sinh bảng kê.
- Xem trước và in thông báo thu phí.
- Ghi nhận thanh toán, xử lý nộp dư và theo dõi công nợ.
- Xem tổng quan và lịch sử giao dịch.

### Ngoài phạm vi

Các phần sau có trong mã nguồn nhưng không nằm trong đợt nghiệm thu này: phương tiện và phí gửi xe, hóa đơn điện nước internet, yêu cầu cư dân, đóng góp tự nguyện, xuất CSV và biến động cư trú. Nhóm chỉ kiểm tra hồi quy để chắc các phần này không làm hỏng luồng thu phí.

## 3. Cách tiếp cận

Nhóm kiểm tra theo bốn lớp, chạy từ thấp đến cao:

1. Kiểm tra đơn vị: công thức tính tiền và quy tắc dữ liệu, chạy tự động bằng JUnit, không cần cơ sở dữ liệu.
2. Kiểm tra tích hợp: chạy toàn bộ luồng nghiệp vụ trên MySQL thật, có tạo và xóa dữ liệu riêng.
3. Kiểm tra giao diện: mở ứng dụng thật, thao tác bằng chuột và bàn phím, chụp ảnh màn hình.
4. Kiểm tra đầu cuối: chạy một kịch bản liên tục từ tạo căn hộ đến xem công nợ, không nhảy bước.

Mỗi lớp có tài liệu riêng: [kịch bản kiểm thử](QA_TEST_SCENARIO.md), [test case chức năng](QA_TEST_CASE_FUNCTIONAL.md), [test case giao diện](QA_TEST_CASE_UI.md), [test case tích hợp](QA_TEST_CASE_INTEGRATION.md), và [dữ liệu kiểm thử](QA_TEST_DATA.md).

## 4. Môi trường

| Thành phần | Yêu cầu |
|---|---|
| Máy chạy giao diện | Windows 10 hoặc 11, màn hình 1920x1080, có máy in PDF |
| Java | JDK 25, kiểm tra bằng `.\mvnw.cmd -version` |
| Maven | Maven Wrapper 3.9.16 kèm trong dự án |
| Cơ sở dữ liệu | MySQL 8.4 chạy bằng Docker, mỗi lượt dùng schema riêng |
| Dữ liệu mẫu | `database/demo-data.sql` nạp tự động khi tạo volume mới |
| Tài khoản thử | Ba tài khoản mẫu trong [dữ liệu kiểm thử](QA_TEST_DATA.md) |

Không chạy kiểm tra tích hợp trên cơ sở dữ liệu đang dùng để demo. Mỗi lượt kiểm tra dùng schema mới hoàn toàn, xong thì xóa.

## 5. Lịch thực hiện

| Tuần | Nội dung | Kết quả cần có |
|---|---|---|
| Tuần 1 | Chốt phạm vi, viết kịch bản và test case | Bộ tài liệu QA được PM duyệt |
| Tuần 2 | Chạy test tự động, sửa lỗi chặn | Báo cáo test tự động không còn lỗi chặn |
| Tuần 3 | Kiểm tra giao diện và in ấn | Ảnh màn hình cho từng màn hình chính |
| Tuần 4 | Chạy kịch bản đầu cuối, tổng hợp kết quả | Biên bản kiểm thử và danh sách lỗi còn mở |

## 6. Phân công

| Vai trò | Người phụ trách | Việc chính |
|---|---|---|
| Kiểm thử | thunganle272 | Viết test case, chạy kiểm tra, ghi biên bản |
| Phát triển | minhduong06-td, Danh Quan, HaTienDat2004 | Sửa lỗi được ghi trong biên bản |
| Duyệt | PhamVanAn268 | Duyệt phạm vi, quyết định lỗi nào phải sửa trước khi nghiệm thu |

## 7. Tiêu chí dừng

Dừng kiểm tra và chuyển sang nghiệm thu khi thỏa cả bốn điều kiện:

1. Không còn lỗi mức cao chưa xử lý.
2. Mọi test case mức ưu tiên 1 đã chạy và đạt.
3. Kịch bản đầu cuối chạy hết một lượt không lỗi chặn.
4. Biên bản kiểm thử có đủ ảnh, log và số liệu đối chiếu.

Nếu còn lỗi mức trung bình, PM quyết định chấp nhận và ghi rõ lý do trong biên bản.

## 8. Rủi ro

| Rủi ro | Ảnh hưởng | Cách giảm |
|---|---|---|
| Máy kiểm tra thiếu JDK 25 | Không biên dịch được, không chạy được test | Kiểm tra `mvnw -version` trước mỗi lượt |
| Dùng nhầm cơ sở dữ liệu demo | Mất dữ liệu demo, kết quả sai | Luôn đặt biến `BLUEMOON_IT_DB_*` riêng |
| Không có máy in thật | Không kiểm tra được bản in giấy | Dùng máy in PDF, lưu tệp làm bằng chứng |
| Dữ liệu thử lẫn với dữ liệu thật | Số liệu đối chiếu không khớp | Dùng bộ dữ liệu riêng trong [dữ liệu kiểm thử](QA_TEST_DATA.md) |
| Thời gian cuối kỳ gấp | Bỏ sót case giao diện | Ưu tiên case mức 1 và kịch bản đầu cuối trước |

## 9. Tài liệu tham chiếu

- [Kịch bản kiểm thử](QA_TEST_SCENARIO.md)
- [Test case chức năng](QA_TEST_CASE_FUNCTIONAL.md)
- [Test case giao diện](QA_TEST_CASE_UI.md)
- [Test case tích hợp](QA_TEST_CASE_INTEGRATION.md)
- [Dữ liệu kiểm thử](QA_TEST_DATA.md)
- [Biên bản kiểm thử](QA_TEST_REPORT.md)
- [Quy tắc nghiệp vụ](NGHIEP_VU.md)
