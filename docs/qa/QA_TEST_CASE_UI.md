# Test case giao diện

Test case giao diện kiểm tra những gì người dùng thật nhìn thấy và thao tác: bố cục, chữ nghĩa, phản hồi khi bấm, và các trường hợp dữ liệu dài hoặc cửa sổ nhỏ.

Ảnh màn hình lưu ở `docs/images/` hoặc `target/ui-review/` làm bằng chứng.

## Màn hình đăng nhập

| Mã | Ưu tiên | Kích thước | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| UI-06 | 1 | 1280x820 | Nhập đúng email và mật khẩu | Vào thẳng tổng quan, menu hiện đúng vai trò |
| UI-07 | 1 | 1280x820 | Nhập sai mật khẩu | Báo lỗi ngắn gọn, không để lộ tài khoản có tồn tại hay không |
| UI-08 | 2 | 1080x700 | Mở màn hình đăng nhập | Logo, ô nhập và nút đăng nhập không bị cắt, không tràn |
| UI-09 | 2 | 1080x700 | Bấm Tab liên tục | Focus đi từ trên xuống, có viền rõ |
| UI-10 | 3 | 1280x820 | Bấm Quên mật khẩu | Hộp thoại mở, nhãn và hướng dẫn đọc được |

## Màn hình tổng quan

| Mã | Ưu tiên | Kích thước | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| UI-11 | 1 | 1280x820 | Mở tổng quan với kỳ 10/2026 đã phát hành | Bốn chỉ số phải thu, đã thu, còn nợ, số hộ hiển thị đúng số liệu |
| UI-12 | 2 | 1080x700 | Mở tổng quan | Các ô chỉ số xuống dòng gọn, không mất số |
| UI-13 | 3 | 1280x820 | Mở tổng quan khi chưa có kỳ thu | Trạng thái trống dễ hiểu, không hiện lỗi |

## Màn hình căn hộ và nhân khẩu

| Mã | Ưu tiên | Kích thước | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| UI-14 | 1 | 1280x820 | Mở danh sách căn hộ | Bảng hiện mã căn, tầng, diện tích, chủ hộ, tình trạng |
| UI-15 | 2 | 1280x820 | Nhập tên hộ dài 120 ký tự | Cột co giãn hợp lý, không cắt mất nút thao tác |
| UI-16 | 2 | 1080x700 | Mở hộp thoại thêm căn hộ | Đủ trường, nút lưu và hủy nhìn thấy, không phải cuộn ngang |
| UI-17 | 3 | 1280x820 | Lọc danh sách theo tầng | Bảng cập nhật ngay, số dòng khớp bộ lọc |

## Màn hình bảng kê và thu tiền

| Mã | Ưu tiên | Kích thước | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| UI-02 | 1 | 1280x820 | Mở bảng kê kỳ 10/2026 | Cột khoản, số lượng, đơn giá, số tiền, đã thu, còn nợ hiện rõ |
| UI-03 | 1 | 1280x820 | Ghi nhận thu tiền cho một khoản | Hộp thoại thu tiền đủ trường, bấm lưu thì bảng cập nhật ngay |
| UI-18 | 1 | 1280x820 | Ghi nhận thu tiền với số tiền lớn hơn số còn nợ | Hộp thoại hiện số cấn và số dư trước khi lưu |
| UI-19 | 2 | 1080x700 | Mở bảng kê ở chiều rộng nhỏ | Thanh công cụ xuống dòng, không mất nút in và lọc |
| UI-20 | 2 | 1280x820 | Nhập số tiền có dấu chấm hoặc chữ | Ô nhập báo lỗi, không lưu dữ liệu sai |
| UI-21 | 3 | 1280x820 | Cuộn bảng kê 16 dòng | Cuộn mượt, tiêu đề cột giữ nguyên |

## Màn hình in thông báo

| Mã | Ưu tiên | Kích thước | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| UI-04 | 1 | 1280x820 | Xem trước thông báo căn A101 | Bố cục đúng, không cắt chữ, số tiền thẳng cột |
| UI-05 | 1 | 1280x820 | In cả kỳ | Có chỉ báo đang xử lý, giao diện không treo, xong thì báo hoàn tất |
| UI-22 | 2 | 1280x820 | Hủy giữa lúc đang in cả kỳ | Dừng đúng, không để lại tệp dở, dùng tiếp được |

## Màn hình công nợ và lịch sử

| Mã | Ưu tiên | Kích thước | Thao tác | Kết quả mong đợi |
|---|---|---|---|---|
| UI-23 | 1 | 1280x820 | Mở công nợ, lọc theo kỳ và trạng thái | Số dòng và số tiền khớp bộ lọc, cột số dư tách khỏi đã thanh toán |
| UI-24 | 2 | 1280x820 | Mở lịch sử thu | Mới nhất trước, đủ người nộp, ngày, hình thức, số tiền |
| UI-25 | 3 | 1280x820 | Mở lịch sử khi chưa có giao dịch | Hiện trạng thái trống, không lỗi |

## Quy ước chung

Case nào yêu cầu hai kích thước thì chụp ảnh ở cả hai. Đặt tên ảnh theo mã case, ví dụ `UI-05-in-ca-ky.png`.

Phát hiện lỗi giao diện thì ghi rõ màn hình, kích thước cửa sổ, bước tái hiện và ảnh chụp. Cắt chữ hoặc mất nút luôn tính là lỗi mức trung bình trở lên.
