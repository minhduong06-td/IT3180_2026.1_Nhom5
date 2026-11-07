# Kịch bản kiểm thử

Kịch bản mô tả một chuỗi thao tác có đầu có cuối, giống như một buổi làm việc thật của ban quản trị. Mỗi kịch bản gồm nhiều test case, chạy theo thứ tự và dùng chung một bộ dữ liệu.

## Danh sách kịch bản

| Mã | Tên | Mục tiêu | Test case liên quan |
|---|---|---|---|
| SC-01 | Chốt kỳ thu tháng đầu tiên | Ban quản trị lập kỳ thu 10/2026 cho toàn bộ căn hộ và kiểm tra bảng kê | FUN-05, FUN-07, FUN-13, UI-02 |
| SC-02 | Thu tiền tại quầy | Kế toán thu tiền của hai hộ, một hộ trả thiếu một hộ trả dư | FUN-08, FUN-09, FUN-10, UI-03 |
| SC-03 | Xử lý nộp dư sang kỳ sau | Kỳ sau tự động trừ số dư còn lại của hộ | FUN-11, FUN-12, INT-03 |
| SC-04 | In và gửi thông báo | In thông báo từng hộ và in cả kỳ, kiểm tra nội dung khớp bảng kê | FUN-13, FUN-27, UI-04, UI-05 |
| SC-05 | Kiểm tra phân quyền | Kế toán không được sửa hồ sơ hay phát hành kỳ, ban quản trị không được thu tiền | FUN-14, INT-08, UI-06 |
| SC-06 | Tiếp nhận hồ sơ mới | Thêm một căn hộ và một hộ mới, phát hành kỳ tiếp theo | FUN-01, FUN-02, FUN-03, FUN-04 |
| SC-07 | Chạy lại sau khi sửa lỗi | Chạy lại kịch bản SC-01 đến SC-03 trên dữ liệu sạch | INT-01, INT-02, INT-04 |

## SC-01: Chốt kỳ thu tháng đầu tiên

**Người thực hiện:** ban quản trị. **Thời gian dự kiến:** 20 phút.

1. Đăng nhập bằng tài khoản ban quản trị.
2. Mở màn hình Khoản thu, kiểm tra có hai khoản bắt buộc đang bật là Phí dịch vụ và Phí quản lý.
3. Mở màn hình Kỳ thu, chọn tháng 10/2026, ngày phát hành 01/10/2026, hạn nộp 15/11/2026.
4. Bấm phát hành, chờ thông báo thành công.
5. Mở bảng kê, kiểm tra tổng số khoản là 16 và tổng tiền là 10.161.750 đồng.
6. Mở chi tiết căn A101, đối chiếu hai khoản 480.000 và 336.000 đồng.
7. Thử phát hành lại tháng 10/2026, xác nhận hệ thống từ chối.

**Kết quả mong đợi:** kỳ thu được tạo một lần, số tiền khớp bảng chuẩn trong [dữ liệu kiểm thử](QA_TEST_DATA.md), lần phát hành thứ hai bị chặn.

## SC-02: Thu tiền tại quầy

**Người thực hiện:** kế toán. **Thời gian dự kiến:** 15 phút.

1. Đăng xuất khỏi tài khoản ban quản trị, đăng nhập bằng tài khoản kế toán.
2. Mở bảng kê, chọn khoản phí dịch vụ của căn A101, ghi nhận 100.000 đồng tiền mặt.
3. Kiểm tra khoản đó còn nợ 380.000 đồng và trạng thái là thu một phần.
4. Thu tiếp 380.000 đồng bằng chuyển khoản, kiểm tra khoản đã đủ.
5. Chọn khoản phí dịch vụ của căn A102, thu 600.000 đồng.
6. Kiểm tra hệ thống cấn 525.000 đồng và giữ lại 75.000 đồng làm số dư.
7. Mở màn hình công nợ, đối chiếu số còn nợ của hai hộ.

**Kết quả mong đợi:** mỗi lần thu tạo đúng một phiếu, số cấn và số dư tách bạch, công nợ cập nhật ngay.

## SC-03: Xử lý nộp dư sang kỳ sau

**Người thực hiện:** ban quản trị. **Thời gian dự kiến:** 10 phút.

1. Đăng nhập lại bằng tài khoản ban quản trị.
2. Phát hành kỳ 11/2026 với ngày phát hành 01/11/2026.
3. Mở bảng kê tháng 11 của căn A102.
4. Kiểm tra hệ thống đã trừ 75.000 đồng số dư, số còn phải thu giảm tương ứng.
5. Kiểm tra số dư của căn A102 về 0.
6. Mở lại bảng kê tháng 10 của căn A102, xác nhận số liệu tháng 10 không bị thay đổi.

**Kết quả mong đợi:** số dư chỉ dùng cho kỳ sau kỳ phát sinh, không cấn ngược kỳ cũ.

## SC-04: In và gửi thông báo

**Người thực hiện:** ban quản trị. **Thời gian dự kiến:** 15 phút.

1. Mở kỳ thu 10/2026, chọn xem trước thông báo của căn A101.
2. Kiểm tra tên hộ, mã căn, danh sách khoản, tổng tiền và hạn nộp.
3. In ra tệp PDF, lưu lại làm bằng chứng.
4. Chọn in cả kỳ, chờ quá trình in hoàn tất.
5. Kiểm tra giao diện không bị treo trong khi in.
6. Mở tệp PDF, đối chiếu số trang bằng số hộ.

**Kết quả mong đợi:** bản in khớp bảng kê, in cả kỳ không treo giao diện, đủ số hộ.

## SC-05: Kiểm tra phân quyền

**Người thực hiện:** kiểm thử. **Thời gian dự kiến:** 10 phút.

1. Đăng nhập bằng tài khoản kế toán.
2. Thử mở màn hình tài khoản và màn hình phát hành kỳ.
3. Xác nhận menu bị ẩn và nếu mở được bằng đường khác thì bị chặn khi lưu.
4. Đăng nhập bằng tài khoản ban quản trị, thử ghi một phiếu thu.
5. Xác nhận thao tác thu tiền bị chặn ở lớp nghiệp vụ.

**Kết quả mong đợi:** quyền được kiểm tra ở lớp xử lý, không chỉ ẩn nút trên giao diện.

## SC-06: Tiếp nhận hồ sơ mới

**Người thực hiện:** ban quản trị. **Thời gian dự kiến:** 20 phút.

1. Tạo căn hộ mới mã P0508, tầng 5, diện tích 80,25 m².
2. Thêm cư dân đầu tiên làm chủ hộ, sau đó thêm một người con.
3. Thử thêm chủ hộ thứ hai, xác nhận bị từ chối.
4. Phát hành kỳ thu mới, kiểm tra căn P0508 xuất hiện trong bảng kê.
5. Đối chiếu số tiền của P0508 với cách tính theo diện tích.

**Kết quả mong đợi:** hồ sơ mới tham gia kỳ thu tiếp theo, ràng buộc một chủ hộ được giữ.

## SC-07: Chạy lại sau khi sửa lỗi

**Người thực hiện:** kiểm thử. **Thời gian dự kiến:** 40 phút.

1. Tạo lại cơ sở dữ liệu sạch từ đầu.
2. Chạy lại SC-01, SC-02, SC-03 theo đúng thứ tự.
3. So sánh kết quả với lần chạy trước, ghi rõ chỗ khác.
4. Chạy bộ test tự động, lưu báo cáo.
5. Ghi kết quả vào [biên bản kiểm thử](QA_TEST_REPORT.md).

**Kết quả mong đợi:** lỗi đã sửa không tái hiện, không phát sinh lỗi mới ở các bước trước đó.
