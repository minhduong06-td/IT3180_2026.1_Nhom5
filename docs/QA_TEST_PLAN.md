# Kế hoạch kiểm thử Sprint 1

Tài liệu do QA lập cho Sprint 1, dùng để đối chiếu với Definition of Done trong `Sprint1.md` và nghiệm thu luồng thu phí.

## Phạm vi

Kiểm thử luồng thu phí tháng 10/2026 từ đầu đến cuối trên nền dữ liệu demo:

1. Tạo căn hộ và nhân khẩu, gán chủ hộ.
2. Khai báo khoản thu bắt buộc và đơn giá có ngày hiệu lực.
3. Phát hành kỳ thu 10/2026, kiểm tra bảng kê từng căn.
4. Xem trước và in thông báo thu phí.
5. Ghi nhận thanh toán, kiểm tra công nợ và số dư nộp trước.
6. Kiểm tra phân quyền giữa ban quản trị và kế toán.

## Ngoài phạm vi

Phương tiện, hóa đơn điện nước, yêu cầu cư dân, xuất CSV và báo cáo thuộc các story sau Sprint 1. Các phần này chỉ kiểm tra hồi quy khi có thay đổi liên quan.

## Môi trường

| Thành phần | Giá trị |
|---|---|
| Hệ điều hành | Windows 11 |
| Java | 25 |
| MySQL | 8.4 qua `docker compose up -d` |
| Dữ liệu | Cơ sở dữ liệu sạch, chạy `DatabaseBootstrap` ở lần khởi động đầu |

## Tiêu chí đạt

| Mã | Nội dung | Kết quả mong đợi |
|---|---|---|
| TC-01 | Đăng nhập sai quá số lần cho phép | Tài khoản bị khóa tạm thời, thông báo rõ thời gian mở lại |
| TC-02 | Tạo căn hộ trùng mã căn | Bị từ chối, dữ liệu cũ không đổi |
| TC-03 | Tạo hai chủ hộ trong cùng căn | Bị từ chối, ràng buộc chủ hộ giữ đúng |
| TC-04 | Tạo kỳ thu trùng tháng | Bị từ chối, kỳ đã phát hành giữ nguyên |
| TC-05 | Phát hành kỳ thu 10/2026 | Sinh đúng số tiền từng căn theo diện tích, nhân khẩu và số xe |
| TC-06 | Sửa đơn giá sau khi phát hành | Bảng kê đã phát hành không đổi |
| TC-07 | Ghi nhận thanh toán nhỏ hơn số phải thu | Công nợ còn lại đúng, lịch sử thu ghi một phiếu |
| TC-08 | Ghi nhận thanh toán lớn hơn số phải thu | Phần dư chuyển thành số dư nộp trước của hộ |
| TC-09 | Kế toán mở màn hình quản lý tài khoản | Bị chặn ở lớp nghiệp vụ, không chỉ ẩn nút |
| TC-10 | In thông báo thu phí cả kỳ | Nội dung khớp bảng kê, giao diện không treo |

## Cách ghi nhận lỗi

Mỗi lỗi ghi theo mẫu: mã test case, bước tái hiện, kết quả thực tế, kết quả mong đợi, mức độ. Lỗi chặn demo phải được dev xác nhận trước ngày chạy demo nội bộ.
