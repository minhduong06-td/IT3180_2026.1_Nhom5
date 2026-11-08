# Test case chức năng

Test case chức năng kiểm tra nghiệp vụ có đúng quy tắc hay không, không quan tâm giao diện đẹp hay xấu. Mỗi case ghi rõ dữ liệu đầu vào, thao tác và kết quả mong đợi để người khác chạy lại được.

Cột ưu tiên: 1 là bắt buộc phải đạt trước nghiệm thu, 2 là nên đạt, 3 là có thể để sau.

## Nhóm đăng nhập và tài khoản

| Mã | Ưu tiên | Dữ liệu đầu vào | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| FUN-14 | 1 | Tài khoản kế toán đang hoạt động | Nhập sai mật khẩu 5 lần, sau đó nhập đúng | Bị khóa ở lần sai thứ 5, nhập đúng vẫn bị chặn, sau 5 phút đăng nhập được |
| FUN-15 | 1 | Tài khoản ban quản trị | Đổi mật khẩu với mật khẩu cũ sai, xác nhận lệch, mật khẩu quá ngắn | Báo lỗi rõ ràng, không đổi mật khẩu, hộp thoại giữ nguyên để sửa |
| FUN-16 | 2 | Tài khoản có email và số điện thoại | Đăng nhập bằng email viết hoa, bằng số điện thoại có khoảng trắng | Vẫn vào được đúng tài khoản |

## Nhóm căn hộ và nhân khẩu

| Mã | Ưu tiên | Dữ liệu đầu vào | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| FUN-01 | 1 | Mã căn P0508, mã hộ HK-QA0508, diện tích 80,25 m² | Tạo căn hộ mới | Lưu thành công, tầng tính ra là 5 |
| FUN-02 | 1 | Mã căn A101 đã tồn tại | Tạo căn hộ trùng mã | Bị từ chối, bản ghi cũ không đổi |
| FUN-03 | 1 | Căn hộ P0508 vừa tạo | Thêm cư dân đầu tiên là chủ hộ, sau đó thêm con | Lưu đúng quan hệ, hộ có một chủ hộ |
| FUN-04 | 1 | Hộ đã có chủ hộ | Thêm người thứ hai làm chủ hộ | Bị từ chối, hộ vẫn một chủ hộ |
| FUN-17 | 2 | Người 14 tuổi chưa có giấy tờ | Lưu nhân khẩu | Bị từ chối vì thiếu giấy tờ |
| FUN-18 | 2 | Ngày chuyển đến trước ngày sinh | Lưu nhân khẩu | Bị từ chối, báo lỗi ngày |

## Nhóm khoản thu và đơn giá

| Mã | Ưu tiên | Dữ liệu đầu vào | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| FUN-05 | 1 | Mã QA_SERVICE, tính theo m², đơn giá 10.000 đồng, hiệu lực 01/01/2026 | Tạo khoản thu mới | Lưu đúng cách tính và đơn giá khởi tạo |
| FUN-06 | 1 | Đơn giá âm, đơn giá 0, thiếu ngày hiệu lực, trùng ngày hiệu lực | Lưu đơn giá | Bị từ chối ở mọi trường hợp sai |
| FUN-19 | 2 | Khoản thu đang tắt | Phát hành kỳ thu | Khoản đang tắt không xuất hiện trong bảng kê |

## Nhóm phát hành kỳ thu

| Mã | Ưu tiên | Dữ liệu đầu vào | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| FUN-07 | 1 | Bộ dữ liệu A đầy đủ | Phát hành kỳ 10/2026, ngày phát hành 01/10/2026 | Sinh 16 khoản, tổng 10.161.750 đồng, mỗi căn đúng số tiền trong bảng chuẩn |
| FUN-20 | 1 | Kỳ 10/2026 đã phát hành | Phát hành lại cùng tháng | Bị từ chối, số khoản và tổng tiền không đổi |
| FUN-21 | 1 | Kỳ 10/2026 đã phát hành | Sửa đơn giá dịch vụ thành 12.000 đồng từ 01/11/2026 | Bảng kê tháng 10 giữ nguyên, chỉ kỳ tháng 11 dùng giá mới |
| FUN-22 | 2 | Khoản bắt buộc chưa có đơn giá hiệu lực | Phát hành kỳ | Báo thiếu đơn giá, không tạo kỳ dở dang |

## Nhóm thanh toán và công nợ

| Mã | Ưu tiên | Dữ liệu đầu vào | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| FUN-08 | 1 | Khoản dịch vụ A101 là 480.000 đồng | Thu 100.000 đồng tiền mặt | Còn nợ 380.000 đồng, trạng thái thu một phần, đúng một phiếu |
| FUN-09 | 1 | Khoản dịch vụ A101 còn 380.000 đồng | Thu tiếp 380.000 đồng chuyển khoản | Khoản đã đủ, có hai phiếu, tổng đã thu 480.000 đồng |
| FUN-10 | 1 | Khoản dịch vụ A102 là 525.000 đồng | Thu 600.000 đồng | Cấn 525.000 đồng, giữ 75.000 đồng làm số dư, khoản đã đủ |
| FUN-11 | 1 | Căn A102 có số dư 75.000 đồng | Phát hành kỳ 11/2026 | Số dư được trừ vào kỳ 11, số phải thu giảm 75.000 đồng |
| FUN-12 | 1 | Số dư phát sinh sau khi kỳ 10 đã phát hành | Mở lại bảng kê kỳ 10 | Số liệu kỳ 10 không thay đổi, không bị cấn ngược |
| FUN-23 | 2 | Khoản đã đủ tiền | Thu thêm cho khoản đó | Bị từ chối, không tạo phiếu mới |
| FUN-24 | 2 | Cùng một khoản, gửi hai lần cùng mã tham chiếu | Thu 100.000 đồng hai lần liên tiếp | Chỉ tạo một phiếu, không tăng tổng đã thu |
| FUN-25 | 1 | Bộ dữ liệu A sau khi thu tiền | Mở màn hình công nợ, lọc theo trạng thái và tầng | Tổng phải thu, đã thu và còn nợ khớp với bảng kê |
| FUN-26 | 2 | Bộ dữ liệu A | Mở tổng quan | Số đã thanh toán tính theo tiền cấn, không cộng toàn bộ tiền nhận |

## Nhóm in thông báo

| Mã | Ưu tiên | Dữ liệu đầu vào | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| FUN-13 | 1 | Kỳ thu 10/2026 đã phát hành | Xem trước thông báo căn A101 | Tên hộ, mã căn, danh sách khoản, tổng tiền và hạn nộp khớp bảng kê |
| FUN-27 | 2 | Kỳ thu có nhiều hộ | In cả kỳ ra PDF | Đủ số trang theo số hộ, giao diện không treo |
| FUN-28 | 3 | Kỳ thu đã phát hành | Đổi tên hộ rồi in lại thông báo cũ | Ghi nhận thông tin nhận diện lấy theo hồ sơ hiện tại |

## Quy ước ghi kết quả

Mỗi lần chạy ghi bốn thông tin: mã case, kết quả (đạt, không đạt, bị chặn, chưa chạy), bằng chứng (ảnh, log hoặc câu truy vấn), và ghi chú ngắn nếu kết quả khác mong đợi.

Không đánh dấu đạt khi chưa chạy thật. Không sửa kết quả mong đợi cho khớp với lỗi đang có.
