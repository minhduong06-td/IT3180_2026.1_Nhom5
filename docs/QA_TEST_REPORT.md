# Biên bản kiểm thử Sprint 1

Ghi nhận kết quả chạy kiểm thử luồng thu phí trên cơ sở dữ liệu sạch, đối chiếu với `QA_TEST_PLAN.md`.

## Lần chạy 1 - 30/10/2026

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

## Lần chạy 2 - 07/11/2026

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

DEF-02 tương ứng nhánh `fix/payment-migration-and-coverage`.

Lỗi cấn trừ số dư vào kỳ cũ đã được phát hiện và sửa trong lúc dev chạy kiểm thử tích hợp ở nhánh `feature/payment-receipts`, trước khi QA nhận bản dựng, nên không ghi thành lỗi riêng ở đây.

## Lần chạy 3 - 08/11/2026

Chạy lại sau khi merge nhánh sửa lỗi, cơ sở dữ liệu sạch và thêm một lượt nâng cấp từ bản cũ.

| Mã | Kết quả | Ghi chú |
|---|---|---|
| TC-01 đến TC-10 | Đạt | Không còn lỗi chặn |

Kiểm thử hồi quy thêm cho hóa đơn tiện ích, phí gửi xe, yêu cầu cư dân và xuất CSV. Không phát hiện lỗi chặn.

## Kết luận

Luồng thu phí tháng 10/2026 đạt Definition of Done của Sprint 1 sau khi sửa DEF-01 và DEF-02. Đủ điều kiện chạy demo nội bộ.
