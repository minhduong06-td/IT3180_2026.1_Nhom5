# BlueMoon

Ứng dụng desktop quản lý căn hộ, cư dân và thu phí chung cư cho ban quản trị và kế toán.

![Tổng quan BlueMoon trên database mới](docs/images/tong-quan.png)

## Chức năng

- Quản lý căn hộ, nhân khẩu, phương tiện và biến động cư trú.
- Thiết lập khoản thu, đơn giá và phát hành kỳ thu theo tháng.
- Tra cứu bảng kê, in thông báo, thu tiền, theo dõi công nợ và số dư nộp trước.
- Quản lý hóa đơn điện, nước, internet, đóng góp tự nguyện và yêu cầu cư dân.
- Quản lý tài khoản, phân quyền, đổi mật khẩu và xuất CSV.

## Cài đặt và chạy

Cần **JDK 25**, Git và Docker Desktop đang chạy. Dự án dùng Maven Wrapper, không cần cài Maven riêng. Lần chạy đầu cần mạng để tải các thư viện.

```powershell
git clone https://github.com/minhduong06-td/IT3180_2026.1_Nhom5.git
Set-Location IT3180_2026.1_Nhom5
.\mvnw.cmd -version
docker compose up -d
docker compose ps
```

Kiểm tra Maven đang dùng Java 25 và chờ MySQL chuyển sang `healthy`, rồi mở ứng dụng:

```powershell
.\mvnw.cmd javafx:run
```

Docker Compose chạy MySQL; cửa sổ desktop được chạy bằng Java trên máy.

Khi tạo volume mới, Docker nạp cấu trúc bảng và dữ liệu khởi tạo từ `database/demo-data.sql`. Ứng dụng mở màn hình đăng nhập với các tài khoản dưới đây; hãy đổi mật khẩu sau lần đăng nhập đầu.

| Vai trò | Tên đăng nhập | Mật khẩu ban đầu |
|---|---|---|
| Ban quản trị | `admin` | `admin123` |
| Kế toán | `accountant` | `accountant123` |

Dữ liệu khởi tạo chỉ được nạp khi Docker tạo volume mới. Ứng dụng vẫn chạy được với database rỗng: tạo database MySQL riêng và đặt các biến kết nối bên dưới. Java tự tạo bảng và yêu cầu tạo quản trị đầu tiên nếu chưa có tài khoản.

### Bắt đầu sử dụng

1. Ban quản trị thêm căn hộ và cư dân.
2. Tạo khoản thu, cách tính và đơn giá có ngày hiệu lực.
3. Tạo tài khoản kế toán, nhập hóa đơn tiện ích nếu có.
4. Phát hành kỳ thu và kiểm tra bảng kê.
5. Kế toán ghi nhận thanh toán, đối chiếu công nợ và lịch sử thu.

Ban quản trị thiết lập và phát hành kỳ; kế toán thu tiền cho khoản phải thu. Cả hai vai trò được xem tổng quan, tự đổi mật khẩu và ghi nhận đóng góp tự nguyện.

## Cấu hình

Cấu hình mặc định khớp với `compose.yaml`:

| Biến | Giá trị mặc định |
|---|---|
| `BLUEMOON_DB_URL` | `jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh` |
| `BLUEMOON_DB_USER` | `bluemoon_app` |
| `BLUEMOON_DB_PASSWORD` | `bluemoon_dev` |

Để dùng MySQL riêng, tạo database và cấp quyền cho user trước, sau đó đặt các biến trên trong cùng cửa sổ PowerShell chạy ứng dụng. Thay biến kết nối không đổi mật khẩu của MySQL trong Docker.

Thông tin chuyển khoản trên thông báo thu phí lấy từ `BLUEMOON_BANK_NAME`, `BLUEMOON_BANK_ACCOUNT`, `BLUEMOON_BANK_HOLDER`. Khi thiếu thông tin, thông báo hướng dẫn liên hệ ban quản trị.

Dừng MySQL và giữ dữ liệu bằng `docker compose down`. Volume lưu dữ liệu qua các lần khởi động; `docker compose down -v` sẽ xóa toàn bộ dữ liệu trong volume.

## Xử lý lỗi khởi động

| Hiện tượng | Kiểm tra |
|---|---|
| `release version 25 not supported`, `Unsupported major.minor version` | Chạy `.\mvnw.cmd -version`; đặt `JAVA_HOME` và `PATH` về JDK 25. Sau đó chạy `.\mvnw.cmd clean javafx:run`. |
| Không kết nối được MySQL | Kiểm tra Docker Desktop, `docker compose ps`, cổng 3307 và các biến `BLUEMOON_DB_*`. Cấu hình user/mật khẩu trong Compose chỉ được áp dụng khi tạo volume lần đầu. |
| Cảnh báo `restricted method` của JavaFX | Cảnh báo native access không phải lỗi kết nối MySQL. Xem thông báo và nguyên nhân lỗi kết nối riêng trên ứng dụng. |

## Công nghệ

Java 25, JavaFX 25.0.2, JDBC, MySQL 8.4, BCrypt và JUnit 5. Maven Wrapper dùng Maven 3.9.16. Phiên bản thư viện được khai báo trong `pom.xml`.

## Kiểm tra và tài liệu

Chạy các test không cần MySQL:

```powershell
.\mvnw.cmd "-Dtest=AuthorizationTest,BillingRulesTest,ResidenceChangeServiceTest,CsvExporterTest,FeeNoticePrinterTest,DemoDataSqlTest" test
```

Test tích hợp cần database riêng, rỗng và có thể xóa. Xem [hướng dẫn QA](docs/QA.md) trước khi chạy; thiếu `BLUEMOON_IT_DB_URL` sẽ làm test tích hợp bị bỏ qua.

- [PM: quyết định merge và điều kiện nghiệm thu](docs/PM.md)
- [QA: cách kiểm tra và căn cứ đạt](docs/QA.md)
- [Dev: cấu trúc, nghiệp vụ và cách sửa](docs/DEV.md)
- [Quy tắc nghiệp vụ chi tiết](docs/NGHIEP_VU.md)
- [Ma trận kiểm tra](docs/QA_TEST_PLAN.md)
- [Kết quả đã ghi nhận](docs/QA_TEST_REPORT.md)

Các lỗi còn mở gồm chống ghi trùng thanh toán, ngày chọn đơn giá, thông tin hộ trên chứng từ cũ, đồng bộ tên chủ hộ và độ trễ khi truy cập MySQL. Chi tiết tại [đối chiếu yêu cầu](docs/SPRINT1_COVERAGE_REVIEW.md).
