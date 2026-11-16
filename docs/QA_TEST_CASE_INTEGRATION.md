# Test case tích hợp

Test case tích hợp kiểm tra các lớp làm việc cùng nhau: giao diện gọi service, service gọi kho dữ liệu, kho dữ liệu chạy trên MySQL thật. Nhóm này bắt được lỗi mà test đơn vị không thấy, ví dụ sai transaction hoặc sai kiểu dữ liệu khi ghi xuống cơ sở dữ liệu.

Mỗi case chạy trên schema riêng, tạo dữ liệu bằng chính ứng dụng chứ không chèn tay.

## Điều kiện chạy

| Thành phần | Giá trị |
|---|---|
| Cơ sở dữ liệu | MySQL 8.4, container riêng cổng 3308 |
| Biến môi trường | `BLUEMOON_IT_DB_URL`, `BLUEMOON_IT_DB_USER`, `BLUEMOON_IT_DB_PASSWORD` |
| Lệnh chạy | `.\mvnw.cmd clean test` |
| Báo cáo | `target/surefire-reports/` |

Nếu thiếu biến môi trường, lớp `MySqlBillingFlowTest` tự bỏ qua. Test bị bỏ qua không được tính là đạt.

## INT-01: Phát hành kỳ thu ghi đúng xuống cơ sở dữ liệu

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | Schema sạch, đã tạo tài khoản ban quản trị và bộ dữ liệu A |
| Thao tác | Gọi `BillingService.issue` cho tháng 10/2026 |
| Kiểm tra | Đếm số dòng bảng `charges`, cộng tổng `amount`, đối chiếu `unit_price_snapshot` |
| Kết quả mong đợi | 16 dòng, tổng 10.161.750 đồng, mỗi dòng có snapshot đúng |
| Bằng chứng | Câu truy vấn đối chiếu và báo cáo Surefire |

## INT-02: Thu tiền một phần rồi thu tiếp

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | INT-01 đã đạt |
| Thao tác | Gọi `CollectionService.recordPayment` hai lần cho cùng một khoản |
| Kiểm tra | Đếm bảng `payments`, cộng `applied_amount`, đọc `amount` và số dư |
| Kết quả mong đợi | Hai phiếu, tổng cấn bằng số phải thu, trạng thái khoản chuyển sang đã thanh toán |
| Bằng chứng | Báo cáo Surefire và câu truy vấn |

## INT-03: Nộp dư sinh số dư và cấn ở kỳ sau

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | INT-01 đã đạt, khoản dịch vụ A102 chưa thu |
| Thao tác | Thu 600.000 đồng cho khoản 525.000 đồng, sau đó phát hành kỳ 11/2026 |
| Kiểm tra | Bảng `apartment_credits` và `credit_applications`, số còn nợ kỳ 11 |
| Kết quả mong đợi | Tạo số dư 75.000 đồng, kỳ 11 cấn đúng 75.000 đồng, số dư còn lại bằng 0 |
| Bằng chứng | Báo cáo Surefire |

## INT-04: Số dư không cấn ngược kỳ đã phát hành

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | Kỳ 10/2026 đã phát hành và đã thu dư ở kỳ 11 |
| Thao tác | Đọc lại số đã thanh toán và số dư của kỳ 10 |
| Kiểm tra | So sánh trước và sau khi phát hành kỳ 11 |
| Kết quả mong đợi | Số liệu kỳ 10 không đổi, số dư không được áp vào kỳ cũ |
| Bằng chứng | Báo cáo Surefire có assertion tương ứng |

## INT-05: Ràng buộc chủ hộ ở mức cơ sở dữ liệu

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | Một căn hộ đã có chủ hộ |
| Thao tác | Gọi `ResidentService.save` để thêm chủ hộ thứ hai |
| Kiểm tra | Bắt ngoại lệ và đếm số chủ hộ trong bảng |
| Kết quả mong đợi | Ném `HouseholdHeadExistsException`, số chủ hộ vẫn là một |
| Bằng chứng | Báo cáo Surefire |

## INT-06: Chặn sửa hóa đơn tiện ích sau khi phát hành

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | Hóa đơn đã nằm trong kỳ đã phát hành |
| Thao tác | Gọi `UtilityService.save` để sửa số tiền |
| Kiểm tra | Ngoại lệ trả về và số tiền trong bảng |
| Kết quả mong đợi | Bị chặn, số tiền cũ giữ nguyên |
| Bằng chứng | Báo cáo Surefire |

## INT-07: Nâng cấp cơ sở dữ liệu cũ

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | Schema cũ có ba căn, chưa có cư dân và tài khoản |
| Thao tác | Chạy bootstrap trên schema cũ hai lần |
| Kiểm tra | Số bảng, số căn, số tài khoản sau mỗi lần |
| Kết quả mong đợi | Giữ nguyên ba căn, tạo được tài khoản quản trị đầu tiên, lần hai không nhân đôi dữ liệu |
| Bằng chứng | Báo cáo Surefire và ảnh chụp bảng |

## INT-08: Kiểm tra phân quyền ở lớp service

| Mục | Nội dung |
|---|---|
| Tiền điều kiện | Có tài khoản kế toán và tài khoản ban quản trị |
| Thao tác | Gọi các hàm quản lý bằng tài khoản kế toán, gọi hàm thu tiền bằng tài khoản ban quản trị |
| Kiểm tra | Ngoại lệ bảo mật và dữ liệu không đổi |
| Kết quả mong đợi | Mọi thao tác trái quyền bị chặn trước khi ghi dữ liệu |
| Bằng chứng | Báo cáo Surefire |

## Bảng tổng hợp

| Mã | Lớp kiểm tra | Cần MySQL | Mức ưu tiên |
|---|---|---|---|
| INT-01 | Phát hành kỳ thu | Có | 1 |
| INT-02 | Thu tiền nhiều lần | Có | 1 |
| INT-03 | Nộp dư sang kỳ sau | Có | 1 |
| INT-04 | Không cấn ngược kỳ cũ | Có | 1 |
| INT-05 | Ràng buộc chủ hộ | Có | 2 |
| INT-06 | Khóa hóa đơn đã phát hành | Có | 2 |
| INT-07 | Nâng cấp cơ sở dữ liệu cũ | Có | 2 |
| INT-08 | Phân quyền ở service | Không | 1 |

## Quy ước

Test tích hợp thay đổi schema và xóa dữ liệu. Luôn dùng cơ sở dữ liệu riêng, không dùng cơ sở dữ liệu demo. Sau mỗi lượt, xóa container và volume để lượt sau bắt đầu sạch.

Khi một case không đạt, ghi lại câu lệnh đã chạy, thông báo lỗi và số liệu thực tế trong [biên bản kiểm thử](QA_TEST_REPORT.md).
