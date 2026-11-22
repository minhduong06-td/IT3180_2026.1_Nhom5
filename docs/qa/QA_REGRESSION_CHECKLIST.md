# Checklist kiểm thử hồi quy - Sprint 1

Chạy checklist này sau khi sửa lỗi hoặc thay bản dựng. Kết quả từng lượt ghi vào `QA_TEST_REPORT.md`, kèm commit, người chạy, môi trường, dữ liệu và bằng chứng. Các ô dưới đây là việc phải kiểm tra, không phải chỗ đánh dấu cho có.

## Chuẩn bị

- [ ] Ghi lại bản dựng, người chạy, JDK/MySQL và phạm vi đợt kiểm tra.
- [ ] Dùng đúng database demo `bluemoon` với dữ liệu trong `demo-data.sql`. Xác nhận với người khác trước khi tạo, sửa hoặc xóa; không chạy song song với ai.
- [ ] Chuẩn bị tài khoản theo vai trò, dữ liệu theo `QA_TEST_DATA.md` và máy in PDF nếu cần kiểm tra in.
- [ ] Ghi trạng thái ban đầu của kỳ, khoản thu và phiếu cần đối chiếu. Không lấy kết quả kỳ khác làm expected.

## Danh sách hồi quy

| Chạy | Mã case liên quan | Kiểm tra và kết quả mong đợi | Bằng chứng cần ghi |
|---|---|---|---|
| [ ] | FUN-14, FUN-15, UI-06 đến UI-08 | Đăng nhập theo vai trò; mật khẩu sai và đổi mật khẩu sai bị xử lý đúng; khóa sau 5 lần sai và mở lại theo quy tắc. | Ảnh hoặc log, kết quả thực tế và vai trò. |
| [ ] | INT-08, SC-05 | Kế toán không làm được việc quản trị; quản trị không ghi được phiếu thu; kiểm tra quyền ở service và menu. | Case/vai trò đã thử, dữ liệu trước và sau. |
| [ ] | FUN-01 đến FUN-04, UI-14 | Tạo và tra cứu căn hộ, nhân khẩu; mã căn trùng và chủ hộ thứ hai bị từ chối. | Ảnh, dữ liệu kiểm tra và kết quả đối chiếu. |
| [ ] | FUN-05 đến FUN-07, FUN-20, INT-01 | Tạo khoản phí, phát hành kỳ, đối chiếu số lượng và tổng tiền; phát hành trùng bị từ chối. | Truy vấn SQL hoặc bảng kê, bản dựng và dữ liệu. |
| [ ] | FUN-21, FUN-29 | Đổi giá giữa tháng rồi phát hành sau ngày đổi; kỳ dùng giá đầu tháng và dữ liệu kỳ cũ không đổi. | Snapshot đơn giá, kỳ, ngày hiệu lực và số tiền. |
| [ ] | FUN-08 đến FUN-12, INT-02 đến INT-04 | Thu một phần, thu đủ, nộp dư và cấn kỳ sau; không cấn ngược kỳ cũ. | Phiếu, số đã cấn, số dư và công nợ trước/sau. |
| [ ] | FUN-24, FUN-30 | Gửi lặp cùng mã giao dịch theo thứ tự và đồng thời; chỉ một phiếu được ghi. | Mã thử, số phiếu, số tiền, log hoặc kết quả. |
| [ ] | FUN-25, FUN-26, UI-11, UI-23 | Đối chiếu tổng phải thu, đã cấn, còn nợ và bộ lọc công nợ với bảng kê. | Ảnh và số liệu đối chiếu. |
| [ ] | FUN-13, FUN-27, UI-04 và UI-05 | Xem thông báo, in PDF từng hộ và cả kỳ; nội dung, số tiền, hạn nộp và số trang phải khớp. | Ảnh hoặc PDF, kỳ và số căn đã kiểm tra. |
| [ ] | FUN-28, R-04 | Đổi hồ sơ sau phát hành, xem và in lại chứng từ cũ; đối chiếu quy tắc lịch sử đã chốt. | Trước/sau và quyết định về expected. |
| [ ] | R-05 | Đổi người liên hệ hoặc chủ hộ, kiểm tra nhãn trên màn hình, công nợ và chứng từ. | Ảnh và quyết định nguồn dữ liệu. |
| [ ] | R-06 | Thử với database chậm, thao tác lặp và in; ghi thời gian phản hồi và dữ liệu phát sinh. | Điều kiện thử, quan sát, log hoặc ảnh. |
| [ ] | INT-07, DEF-04 và DEF-05 | Kiểm tra nâng cấp schema cũ, dữ liệu còn nguyên và số dư không cấn ngược. | Schema trước/sau và kết quả truy vấn. |
| [ ] | SC-01 đến SC-05 | Chạy lại luồng chính liền mạch và kiểm tra phân quyền sau các thay đổi. | Danh sách bước, kết quả, lỗi và ảnh/log. |

## Ghi kết quả

Mỗi dòng dùng một trong bốn trạng thái **Đạt / Không đạt / Bị chặn / Chưa chạy**, ghi mã lỗi nếu có và trỏ tới kết quả trong biên bản. Test bị bỏ qua ghi là Chưa chạy. Chạy lại trước các case dính trực tiếp tới lỗi vừa sửa, rồi mới chạy luồng thu phí chính. Kết luận nghiệm thu theo tiêu chí trong `QA_TEST_PLAN.md`.
