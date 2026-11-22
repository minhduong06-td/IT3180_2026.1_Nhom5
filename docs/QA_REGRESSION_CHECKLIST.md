# Checklist kiểm thử hồi quy - Sprint 1

Dùng sau khi sửa lỗi hoặc thay đổi bản dựng. Ghi kết quả từng lần chạy vào `QA_TEST_REPORT.md` với commit/bản dựng, người chạy, môi trường, dữ liệu và evidence. Các ô dưới đây là danh sách cần kiểm tra, không phải khẳng định đã đạt trên bản hiện tại.

## Chuẩn bị

- [ ] Xác định bản dựng, người chạy, JDK/MySQL và phạm vi đợt kiểm tra.
- [ ] Dùng database và dữ liệu được phép kiểm thử; xác nhận trước các thao tác tạo, sửa và xóa. Không chạy test tích hợp thay đổi schema trên dữ liệu đang sử dụng.
- [ ] Chuẩn bị tài khoản theo vai trò, dữ liệu theo `QA_TEST_DATA.md` và máy in PDF nếu kiểm tra in.
- [ ] Ghi trạng thái ban đầu của kỳ, khoản thu và phiếu cần đối chiếu; không dùng kết quả kỳ khác làm expected.

## Danh sách hồi quy

| Chạy | Mã case liên quan | Kiểm tra và kết quả mong đợi | Evidence cần ghi |
|---|---|---|---|
| [ ] | FUN-14, FUN-15, UI-06-08 | Đăng nhập theo vai trò; mật khẩu sai và đổi mật khẩu sai bị xử lý đúng; khóa sau 5 lần sai và mở lại theo quy tắc. | Ảnh hoặc log, kết quả thực tế và vai trò. |
| [ ] | INT-08, SC-05 | Kế toán không thực hiện tác vụ quản trị; quản trị không ghi phiếu thu; kiểm tra quyền ở service và menu. | Case/role được thử và dữ liệu trước/sau. |
| [ ] | FUN-01-04, UI-14 | Tạo, tra cứu căn hộ/nhân khẩu; mã căn trùng và chủ hộ thứ hai bị từ chối. | Ảnh, dữ liệu kiểm tra và kết quả đối chiếu. |
| [ ] | FUN-05-07, FUN-20, INT-01 | Tạo khoản phí, phát hành kỳ, đối chiếu số lượng/tổng tiền; phát hành trùng bị từ chối. | Truy vấn SQL hoặc bảng kê, bản dựng và dữ liệu. |
| [ ] | FUN-21, FUN-29 | Đổi giá giữa tháng và phát hành sau ngày đổi; kỳ dùng giá đầu tháng và dữ liệu kỳ cũ không đổi. | Snapshot đơn giá, kỳ, ngày hiệu lực và số tiền. |
| [ ] | FUN-08-12, INT-02-04 | Thu một phần, thu đủ, nộp dư và cấn kỳ sau; không cấn ngược kỳ cũ. | Phiếu, số đã cấn, số dư và công nợ trước/sau. |
| [ ] | FUN-24, FUN-30 | Gửi lặp cùng mã giao dịch theo thứ tự và đồng thời; chỉ một phiếu được ghi nhận. | Mã thử, số phiếu, số tiền, log/kết quả. |
| [ ] | FUN-25, FUN-26, UI-11, UI-23 | Đối chiếu tổng phải thu, đã cấn, còn nợ và bộ lọc công nợ với bảng kê. | Ảnh và số liệu đối chiếu. |
| [ ] | FUN-13, FUN-27, UI-04-05 | Xem thông báo, in PDF từng hộ/cả kỳ; nội dung, số tiền, hạn nộp và số trang phù hợp. | Ảnh/PDF, kỳ và số căn đã kiểm tra. |
| [ ] | FUN-28, R-04 | Đổi hồ sơ sau phát hành, xem/in lại chứng từ cũ; đối chiếu quy tắc lịch sử đã được chốt. | Trước/sau và quyết định về expected. |
| [ ] | R-05 | Đổi người liên hệ/chủ hộ và kiểm tra nhãn trên màn hình, công nợ, chứng từ. | Ảnh và quyết định nguồn dữ liệu. |
| [ ] | R-06 | Thử truy cập database chậm, thao tác lặp và in; ghi thời gian phản hồi và dữ liệu phát sinh. | Điều kiện thử, quan sát, log/ảnh. |
| [ ] | INT-07, DEF-04-05 | Kiểm tra nâng cấp schema cũ, dữ liệu còn nguyên và số dư không cấn ngược. | Bản schema trước/sau và kết quả truy vấn. |
| [ ] | SC-01-05 | Chạy lại luồng chính liên tục và phân quyền sau các thay đổi. | Danh sách bước, kết quả, lỗi và ảnh/log. |

## Ghi kết quả

Với mỗi dòng, dùng một trong bốn trạng thái **Đạt / Không đạt / Bị chặn / Chưa chạy**, ghi mã lỗi nếu có và liên kết đến kết quả trong biên bản. Test bị bỏ qua không được tính là đạt. Ưu tiên chạy lại các case liên quan trực tiếp đến lỗi đã sửa, sau đó chạy luồng thu phí chính; kết luận nghiệm thu theo tiêu chí trong `QA_TEST_PLAN.md`.
