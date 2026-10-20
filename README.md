# BlueMoon

Ứng dụng máy tính quản lý căn hộ, khoản thu, công nợ và thanh toán cho ban quản trị chung cư.

## Chức năng

- Quản lý căn hộ, diện tích, chủ hộ và thông tin liên hệ.
- Quản lý khoản thu và đơn giá có ngày hiệu lực.
- Phát hành kỳ thu, tạo bảng kê chi tiết theo từng căn hộ.
- Ghi nhận thanh toán một phần hoặc toàn bộ bằng tiền mặt hay chuyển khoản.
- Theo dõi số phải thu, số đã thu, công nợ và lịch sử giao dịch.
- Phân quyền thao tác giữa ban quản trị và kế toán.

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

Ví dụ trong PowerShell:

```powershell
$env:BLUEMOON_DB_URL = 'jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_DB_USER = 'bluemoon_app'
$env:BLUEMOON_DB_PASSWORD = 'mat-khau-cua-ban'
./mvnw.cmd javafx:run
```

Chi tiết quy trình sử dụng và quy tắc thu phí nằm trong [Hướng dẫn nghiệp vụ](docs/NGHIEP_VU.md).
