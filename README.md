# BlueMoon

Ứng dụng máy tính quản lý căn hộ, khoản thu, công nợ và thanh toán cho ban quản trị chung cư.

## Chức năng

- Quản lý hộ khẩu, căn hộ và nhân khẩu; tra cứu theo căn hộ, tầng, họ tên, giấy tờ hoặc điện thoại.
- Phân loại khoản bắt buộc, tính phí theo diện tích, nhân khẩu hoặc căn hộ; lưu khoản đóng góp theo số tiền thực nhận.
- Quản lý đơn giá có ngày hiệu lực và giữ nguyên giá trị của các kỳ đã phát hành.
- Phát hành kỳ thu, tạo bảng kê chi tiết theo từng căn hộ.
- Xem trước và in thông báo thu phí cho từng căn hộ hoặc cả kỳ thu.
- Ghi nhận thanh toán một phần hoặc toàn bộ bằng tiền mặt hay chuyển khoản.
- Theo dõi riêng lịch sử đóng góp tự nguyện, không cộng vào công nợ phải thu.
- Theo dõi số phải thu, số đã thu, công nợ và lịch sử giao dịch.
- Phân quyền thao tác giữa ban quản trị và kế toán.
- Quản lý tài khoản, đổi mật khẩu và khóa đăng nhập tạm thời khi nhập sai nhiều lần.

## Công nghệ

- Java 25 và JavaFX 25
- Maven Wrapper 3.9.16
- MySQL 8.4
- JDBC, MySQL Connector/J và BCrypt

## Khởi động trên Windows

Cài Docker Desktop, sau đó mở PowerShell tại thư mục dự án:

```powershell
docker compose up -d
./mvnw.cmd javafx:run
```

Lần chạy đầu tiên sẽ tạo cấu trúc cơ sở dữ liệu cùng tài khoản, căn hộ, khoản thu và đơn giá khởi tạo. Để dừng cơ sở dữ liệu:

```powershell
docker compose down
```

Để xóa toàn bộ dữ liệu cục bộ và khởi tạo lại từ đầu:

```powershell
docker compose down -v
```

## Tài khoản khởi tạo

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Ban quản trị | `admin` | `admin123` |
| Kế toán | `accountant` | `accountant123` |

## Cấu hình cơ sở dữ liệu

Mặc định ứng dụng kết nối MySQL tại `localhost:3307`, cơ sở dữ liệu `bluemoon`. Có thể ghi đè bằng các biến môi trường:

| Biến | Giá trị mặc định |
|---|---|
| `BLUEMOON_DB_URL` | `jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh` |
| `BLUEMOON_DB_USER` | `bluemoon_app` |
| `BLUEMOON_DB_PASSWORD` | `bluemoon_dev` |

Thông tin nhận chuyển khoản trên thông báo có thể đặt qua `BLUEMOON_BANK_NAME`,
`BLUEMOON_BANK_ACCOUNT` và `BLUEMOON_BANK_HOLDER`. Nếu chưa cấu hình, thông báo
hướng người nộp liên hệ Ban quản trị.

Ví dụ trong PowerShell:

```powershell
$env:BLUEMOON_DB_URL = 'jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_DB_USER = 'bluemoon_app'
$env:BLUEMOON_DB_PASSWORD = 'mat-khau-cua-ban'
./mvnw.cmd javafx:run
```

Chi tiết quy trình sử dụng và quy tắc thu phí nằm trong [Hướng dẫn nghiệp vụ](docs/NGHIEP_VU.md).
