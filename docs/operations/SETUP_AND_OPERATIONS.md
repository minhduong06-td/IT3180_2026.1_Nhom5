# Cài đặt và vận hành BlueMoon

Hướng dẫn chạy BlueMoon trong môi trường học tập hoặc demo trên Windows. Đây không phải hướng dẫn đưa hệ thống lên máy chủ dùng dữ liệu thật.

## Yêu cầu

- Windows với PowerShell.
- JDK 25.
- Docker Desktop đang chạy.
- Git và mạng cho lần build đầu để tải thư viện Maven.

Dự án có Maven Wrapper nên không cần cài Maven riêng.

## Chạy lần đầu

Mở PowerShell trong thư mục repo:

```powershell
.\mvnw.cmd -version
docker compose up -d mysql
docker compose ps
```

Chờ MySQL báo `healthy` rồi chạy ứng dụng:

```powershell
.\mvnw.cmd javafx:run
```

MySQL chạy ở `localhost:3307`, database `bluemoon`. Volume mới thì Docker nạp cấu trúc từ `src/main/resources/db/schema.sql` và dữ liệu demo từ `database/demo-data.sql`. Tài khoản mẫu ghi trong README gốc.

## Chạy lại

```powershell
docker compose up -d mysql
docker compose ps
.\mvnw.cmd javafx:run
```

Ứng dụng không kết nối được thì kiểm tra Docker Desktop, trạng thái MySQL và cổng `3307`. Cấu hình đọc từ `BLUEMOON_DB_URL`, `BLUEMOON_DB_USER`, `BLUEMOON_DB_PASSWORD`; không đặt biến nào thì ứng dụng dùng cấu hình local mặc định.

## Giữ và tạo lại dữ liệu

Volume `bluemoon-nhom5-data` chứa database. `docker compose down` dừng và xóa container nhưng giữ volume. **Đừng chạy `docker compose down -v` nếu cần giữ dữ liệu**, vì `-v` xóa volume.

Trước khi đổi schema hoặc xóa dữ liệu, sao lưu bằng `mysqldump` hoặc công cụ quản trị MySQL. Lưu bản sao ngoài repo và thử đọc lại trước khi xóa dữ liệu gốc.

Muốn xóa sạch và tạo lại database demo từ đầu thì chắc chắn không còn dữ liệu cần giữ, rồi chạy:

```powershell
docker compose down -v
docker compose up -d mysql
```

Volume mới sẽ được khởi tạo lại từ schema và dữ liệu demo. Dữ liệu demo không phải bản sao lưu.

## Kết nối tới MySQL riêng

Đặt thông tin kết nối trong đúng cửa sổ PowerShell chạy ứng dụng:

```powershell
$env:BLUEMOON_DB_URL = 'jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_DB_USER = 'bluemoon_app'
$env:BLUEMOON_DB_PASSWORD = 'mat-khau-cua-ban'
.\mvnw.cmd javafx:run
```

Thay bằng cấu hình database bạn quản lý. Không đưa mật khẩu thật vào file được commit. Xong việc thì xóa biến:

```powershell
Remove-Item Env:BLUEMOON_DB_URL, Env:BLUEMOON_DB_USER, Env:BLUEMOON_DB_PASSWORD
```

Thông tin trong `compose.yaml` chỉ dùng cho local/demo, không dùng với dữ liệu thật.

## Cập nhật và kiểm tra

- Sao lưu database trước khi đổi schema hoặc cập nhật phiên bản.
- Đọc thay đổi trong `src/main/resources/db/schema.sql` và `DatabaseBootstrap` trước khi cập nhật dữ liệu cũ. Script khởi tạo của Docker chỉ chạy lúc tạo volume mới.
- Kiểm tra theo [hướng dẫn QA](../qa/QA.md). Quy trình hiện tại dùng database demo ở cổng `3307`; đừng chạy test có ghi dữ liệu khi người khác đang dùng database đó.
- Báo lỗi thì ghi phiên bản Java, commit đang chạy, trạng thái container và thao tác dẫn tới lỗi. Đừng gửi mật khẩu hay thông tin cá nhân vào báo cáo.
