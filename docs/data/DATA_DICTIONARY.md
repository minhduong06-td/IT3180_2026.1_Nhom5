# Từ điển dữ liệu BlueMoon

Tài liệu giải thích mục đích các bảng trong database. Tên cột, kiểu dữ liệu, giá trị enum, khóa và ràng buộc chính xác nằm trong [`schema.sql`](../../src/main/resources/db/schema.sql); lệch nhau thì theo schema.

## Tài khoản và hồ sơ

| Bảng | Lưu gì | Liên hệ chính |
|---|---|---|
| `app_users` | Tài khoản, thông tin đăng nhập, vai trò và trạng thái. | Tài khoản cư dân có thể gắn với căn hộ; các bảng nghiệp vụ tham chiếu tới người ghi nhận. |
| `account_login_state` | Số lần đăng nhập sai và thời điểm hết khóa tạm. | Một dòng cho mỗi tài khoản; xóa cùng tài khoản. |
| `apartments` | Mã căn, hộ khẩu, diện tích, liên hệ, ngày nhận nhà và tình trạng căn. | Hồ sơ gốc cho cư dân, phương tiện, hóa đơn và các khoản theo căn. |
| `residents` | Nhân khẩu, giấy tờ, quan hệ với chủ hộ và ngày chuyển đến. | Mỗi người thuộc một căn hộ; một căn hộ có tối đa một chủ hộ. |
| `residence_changes` | Lịch sử thường trú, tạm trú hoặc tạm vắng và người ghi nhận. | Gắn với cư dân và tài khoản ghi nhận; hiện chưa dùng để tính số nhân khẩu. |
| `vehicles` | Loại xe, biển số và trạng thái đăng ký. | Thuộc một căn hộ; biển số đang hoạt động phải duy nhất. |

## Khoản thu và kỳ thu

| Bảng | Lưu gì | Liên hệ chính |
|---|---|---|
| `fee_types` | Danh mục khoản thu, bắt buộc/tự nguyện, cách tính và trạng thái. | Có thể có nhiều đơn giá; bảng kê hoặc đóng góp tham chiếu tới. |
| `fee_rates` | Đơn giá và ngày bắt đầu áp dụng cho khoản thu. | Một khoản thu không có hai đơn giá cùng ngày hiệu lực. |
| `utility_bills` | Hóa đơn điện, nước hoặc internet theo căn và tháng. | Mỗi căn có tối đa một hóa đơn cho cùng tháng và loại tiện ích. |
| `billing_periods` | Tháng thu, ngày phát hành, hạn nộp và người tạo. | Mỗi tháng/năm chỉ có một kỳ; một kỳ có nhiều bảng kê. |
| `charges` | Khoản phải thu của căn hộ trong kỳ, gồm cách tính, số lượng, đơn giá và số tiền lúc phát hành. | Gắn với kỳ, căn hộ và loại phí; đây là đối tượng được thanh toán. |

Các trường `_snapshot` trong `charges` lưu dữ liệu dùng để tính tiền lúc phát hành, không phải bản chụp toàn bộ hồ sơ căn hộ. Mã căn và tên hộ vẫn đọc từ hồ sơ hiện tại khi hiển thị.

## Thanh toán và khoản thu riêng

| Bảng | Lưu gì | Liên hệ chính |
|---|---|---|
| `payments` | Phiếu thu, người nộp, số tiền nhận, số tiền đã cấn, hình thức và người ghi nhận. | Mỗi phiếu gắn với một khoản phải thu. `amount` là tiền nhận; `applied_amount` là phần cấn vào khoản đó. |
| `apartment_credits` | Phần tiền nộp dư còn lại của căn hộ. | Sinh ra từ một phiếu thu; số dư giảm dần khi được dùng cho kỳ sau. |
| `credit_applications` | Lần dùng số dư để thanh toán khoản phải thu. | Nối một khoản dư với một khoản phải thu và lưu số tiền đã dùng. |
| `contributions` | Tiền đóng góp tự nguyện đã nhận. | Gắn với căn hộ, loại đóng góp và người ghi nhận; không tạo công nợ bắt buộc. |

## Yêu cầu cư dân

| Bảng | Lưu gì | Liên hệ chính |
|---|---|---|
| `resident_requests` | Nội dung yêu cầu, phân loại, trạng thái và ghi chú xử lý. | Gắn với căn hộ và tài khoản tạo yêu cầu. Chỉ lưu trạng thái hiện tại, không lưu từng lần chuyển trạng thái. |

## Mối liên hệ tổng quát

```text
apartments ── residents ── residence_changes
     ├────── vehicles
     ├────── utility_bills
     ├────── charges ── billing_periods
     │            └──── payments ── apartment_credits ── credit_applications
     ├────── contributions
     └────── resident_requests

fee_types ── fee_rates
     ├────── charges
     └────── contributions
```

ERD dạng hình nằm trong [thư mục sơ đồ](../diagram/README.md). Dữ liệu demo nằm trong `database/demo-data.sql`; đổi cấu trúc bảng thì cập nhật schema và quy trình nâng cấp theo.
