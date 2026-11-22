# Dữ liệu kiểm thử

Tài liệu mô tả các bộ dữ liệu dùng cho kiểm thử, cách tạo lại và số liệu chuẩn để đối chiếu.

Nguyên tắc chung: chỉ dùng database demo `bluemoon` của project. Docker nạp `src/main/resources/db/schema.sql` rồi `database/demo-data.sql` khi tạo volume lần đầu. Không tạo schema, database hoặc container thứ hai để thay thế dữ liệu này.

## Bộ A: dữ liệu đối chiếu tính tiền

Dùng cho các case tính phí, thanh toán, công nợ, in và tổng quan.

### Căn hộ

| Mã căn | Mã hộ | Diện tích (m²) | Chủ hộ | Tình trạng |
|---|---|---:|---|---|
| A101 | HK-000101 | 48,00 | Nguyễn Minh An | Đang ở |
| A102 | HK-000102 | 52,50 | Trần Gia Bảo | Đang ở |
| A103 | HK-000103 | 65,00 | Lê Thu Hà | Đang ở |
| A104 | HK-000104 | 70,00 | Phạm Quốc Huy | Đang ở |
| A105 | HK-000105 | 78,50 | Đỗ Mai Anh | Đang ở |
| A106 | HK-000106 | 86,25 | Vũ Hoàng Nam | Đang ở |
| A107 | HK-000107 | 92,00 | Nguyễn Lan Chi | Đang ở |
| A108 | HK-000108 | 105,50 | Bùi Đức Long | Đang ở |

Tổng diện tích: 597,75 m². Tổng 18 nhân khẩu, gồm đủ quan hệ chủ hộ, vợ chồng, con, bố mẹ và người thuê.

### Khoản thu

| Mã | Tên | Bắt buộc | Cách tính | Đơn giá | Hiệu lực |
|---|---|---|---|---:|---|
| SERVICE | Phí dịch vụ | Có | Theo m² | 10.000 | 01/01/2026 |
| MANAGEMENT | Phí quản lý | Có | Theo m² | 7.000 | 01/01/2026 |
| SANITATION | Phí vệ sinh | Không | Theo nhân khẩu | 6.000 | 01/01/2026 |
| MOTORCYCLE_PARKING | Phí gửi xe máy | Không | Theo xe máy | 70.000 | 01/01/2026 |
| CAR_PARKING | Phí gửi ô tô | Không | Theo ô tô | 1.200.000 | 01/01/2026 |

Chỉ hai khoản bắt buộc được bật khi phát hành. Ba khoản còn lại để tắt, chỉ bật khi kiểm tra phần mở rộng.

### Số tiền chuẩn kỳ 10/2026

Phát hành ngày 01/10/2026, hạn nộp 15/11/2026.

| Căn | Diện tích | Phí dịch vụ | Phí quản lý | Tổng |
|---|---:|---:|---:|---:|
| A101 | 48,00 | 480.000 | 336.000 | 816.000 |
| A102 | 52,50 | 525.000 | 367.500 | 892.500 |
| A103 | 65,00 | 650.000 | 455.000 | 1.105.000 |
| A104 | 70,00 | 700.000 | 490.000 | 1.190.000 |
| A105 | 78,50 | 785.000 | 549.500 | 1.334.500 |
| A106 | 86,25 | 862.500 | 603.750 | 1.466.250 |
| A107 | 92,00 | 920.000 | 644.000 | 1.564.000 |
| A108 | 105,50 | 1.055.000 | 738.500 | 1.793.500 |
| Tổng | 597,75 | 5.977.500 | 4.184.250 | 10.161.750 |

Kỳ này sinh 16 khoản phải thu, 8 hộ, tất cả chưa thanh toán.

### Kịch bản thu tiền chuẩn

| Bước | Hộ | Khoản | Số tiền nhận | Cấn | Dư | Còn nợ hộ |
|---|---|---:|---:|---:|---:|---:|
| 1 | A101 | Dịch vụ | 100.000 | 100.000 | 0 | 716.000 |
| 2 | A101 | Dịch vụ | 380.000 | 380.000 | 0 | 336.000 |
| 3 | A102 | Dịch vụ | 600.000 | 525.000 | 75.000 | 367.500 |

Sau ba bước: tổng tiền nhận 1.080.000 đồng, tổng đã cấn 1.005.000 đồng, tổng còn nợ 9.156.750 đồng, số dư của A102 là 75.000 đồng.

## Bộ B: dữ liệu cho kiểm tra tạo mới

Dùng cho các case tạo và sửa hồ sơ, kiểm tra trùng lặp và ràng buộc dữ liệu.

| Loại | Giá trị dùng cho kiểm tra |
|---|---|
| Mã căn | P0508 |
| Mã hộ | HK-QA0508 |
| Diện tích | 80,25 m² |
| Tài khoản kế toán | qa_accountant |
| Mã khoản thu | QA_SERVICE |
| Số giấy tờ | Chuỗi 12 chữ số chưa dùng trong bộ A |

Bộ này là số liệu tham chiếu trong database demo. Không cộng lại hoặc áp dụng các số liệu này cho một database khác.

## Bộ C: dữ liệu cho phần mở rộng

Dùng khi kiểm tra hồi quy các phần ngoài phạm vi nghiệm thu.

| Thành phần | Số lượng | Ghi chú |
|---|---|---|
| Xe máy | 2 | Một xe đang hoạt động, một xe đã ngừng |
| Ô tô | 1 | Đang hoạt động |
| Hóa đơn điện | 1 | Theo căn và kỳ |
| Hóa đơn nước | 1 | Theo căn và kỳ |
| Đóng góp | 2 | Một lần bị từ chối vì bằng 0, một lần hợp lệ |
| Yêu cầu cư dân | 1 | Từ tiếp nhận đến hoàn tất |

## Bộ D: dữ liệu nâng cấp

Dùng để kiểm tra việc nâng cấp từ phiên bản cũ.

- Schema cũ có ba căn A101 đến A103, chưa có cư dân và tài khoản.
- Một biến thể có giao dịch thanh toán thiếu trường người nộp và số đã cấn.
- Không chạy nâng cấp trên database khác để thay thế database demo; ghi rõ trạng thái dữ liệu demo trước mỗi lần kiểm tra.

## Bộ E: dữ liệu cho case biên

| Mã | Mục đích | Chuẩn bị |
|---|---|---|
| E-1 | Thay đổi đơn giá giữa tháng | Bộ A, thêm đơn giá 12.000 đồng từ 15/10/2026 |
| E-2 | Đổi hồ sơ sau khi phát hành | Bộ A, đổi tên hộ sau khi in thông báo |
| E-3 | Gửi trùng yêu cầu thu | Bộ A, gửi hai lần cùng mã tham chiếu |
| E-4 | Cơ sở dữ liệu chậm | Bộ A, thêm độ trễ khi đọc để kiểm tra phản hồi giao diện |

Mỗi case biên dùng một bản sao độc lập của bộ A, khôi phục lại trước mỗi lần chạy.

## Tài khoản dùng cho kiểm thử

| Vai trò | Email | Số điện thoại | Mật khẩu ban đầu |
|---|---|---|---|
| Ban quản trị | admin@bluemoon.vn | 0901000001 | admin123 |
| Kế toán | ngan@bluemoon.vn | 0901000002 | accountant123 |
| Cư dân | minhan@gmail.com | 0901000003 | resident123 |

Đổi mật khẩu sau lần đăng nhập đầu tiên khi chạy trên môi trường dùng chung.

## Cách tạo lại dữ liệu

Khi tạo volume mới, Docker tự nạp `src/main/resources/db/schema.sql` và `database/demo-data.sql`. Đây là dữ liệu dùng cho demo và cho quy trình QA hiện tại; phải ghi lại trạng thái ban đầu trước khi chạy case có ghi dữ liệu.

Bộ A là bộ đối chiếu nghiệp vụ; SC-06 chỉ tạo một căn P0508 và không tạo đủ bộ A. Khi chạy trên database demo, chỉ đối chiếu các dòng thực tế đang có và ghi rõ dữ liệu đầu vào; không tự chuyển sang schema trống.

Không dùng `docker compose down -v` để chuẩn bị kiểm thử, vì lệnh đó xóa volume dữ liệu dự án. Các truy vấn chỉ đọc có thể thực hiện trên database ứng dụng hiện có; ghi lại trạng thái ban đầu và dữ liệu thực tế thay vì áp số chuẩn bộ A cho dữ liệu khác.

## Câu truy vấn đối chiếu

Chạy trên database demo `bluemoon`, chỉ đọc dữ liệu.

```sql
SELECT COUNT(*) AS apartment_count FROM apartments;
SELECT COUNT(*) AS resident_count FROM residents;

SELECT p.period_year, p.period_month, COUNT(c.id) AS charge_count,
       COALESCE(SUM(c.amount), 0) AS total_amount
FROM billing_periods p
LEFT JOIN charges c ON c.billing_period_id = p.id
GROUP BY p.id, p.period_year, p.period_month
ORDER BY p.period_year, p.period_month;

SELECT a.code, f.code AS fee_code, c.quantity_snapshot,
       c.unit_price_snapshot, c.amount
FROM charges c
JOIN billing_periods p ON p.id = c.billing_period_id
JOIN apartments a ON a.id = c.apartment_id
JOIN fee_types f ON f.id = c.fee_type_id
WHERE p.period_year = 2026 AND p.period_month = 10
ORDER BY a.code, f.code;

SELECT a.code, SUM(ac.remaining_amount) AS credit_remaining
FROM apartment_credits ac
JOIN apartments a ON a.id = ac.apartment_id
GROUP BY a.id, a.code;
```

Không join trực tiếp bảng thanh toán với bảng cấn trừ rồi cộng, vì số tiền sẽ bị nhân lên. Tổng đã thanh toán lấy từ số cấn, không lấy từ số tiền nhận.
