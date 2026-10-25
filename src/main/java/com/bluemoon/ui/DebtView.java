package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.BillingPeriod;
import com.bluemoon.model.Domain.ChargeStatus;
import com.bluemoon.model.Domain.HouseholdDebt;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class DebtView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final ComboBox<String> periodFilter = new ComboBox<>();
    private final Map<String, Long> periodIds = new HashMap<>();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> floorFrom = floorPicker();
    private final ComboBox<String> floorTo = floorPicker();
    private final TableView<HouseholdDebt> table = new TableView<>();
    private final HBox summaries = new HBox(12);
    private final Label footer = Ui.label("", "muted");

    DebtView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        periodFilter.setPromptText("Chọn kỳ thu");
        periodFilter.getStyleClass().add("form-field");
        statusFilter.getItems().setAll("Tất cả trạng thái", "Còn nợ", "Chưa đóng", "Đóng thiếu", "Đã đóng đủ");
        statusFilter.setValue("Tất cả trạng thái");
        statusFilter.getStyleClass().add("form-field");
        var filter = Ui.button("Lọc công nợ", true);
        filter.setOnAction(event -> load());
        table.setPlaceholder(new Label("Không có hộ phù hợp với bộ lọc."));
        summaries.getChildren().addAll(
                metric("Tổng phải thu", "0 ₫", ""),
                metric("Đã thanh toán", "0 ₫", "summary-accent"),
                metric("Còn nợ", "0 ₫", "summary-warn"),
                metric("Hộ đã hoàn tất", "0/0", ""));
        for (var node : summaries.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);
        getChildren().addAll(Ui.pageHeader("Theo dõi công nợ", "Tổng hợp số phải thu, thanh toán và số dư theo từng hộ."),
                Ui.toolbar(periodFilter, statusFilter, floorFrom, floorTo, filter), summaries, table, footer);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        loadPeriods();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Căn hộ", HouseholdDebt::apartmentCode));
        table.getColumns().add(column("Chủ hộ", HouseholdDebt::householdName));
        table.getColumns().add(column("Tầng", row -> Integer.toString(row.floor())));
        table.getColumns().add(column("Diện tích", row -> row.areaM2().stripTrailingZeros().toPlainString() + " m²"));
        table.getColumns().add(column("Nhân khẩu", row -> Integer.toString(row.residentCount())));
        table.getColumns().add(column("Phải thu", row -> Ui.money(row.totalDue())));
        table.getColumns().add(column("Đã thanh toán", row -> Ui.money(row.paid())));
        table.getColumns().add(column("Còn nợ", row -> Ui.money(row.balance())));
        table.getColumns().add(column("Số dư nộp trước", row -> Ui.money(row.creditBalance())));
        table.getColumns().add(column("Trạng thái", row -> status(row.status())));
        table.getColumns().add(column("Lần thu cuối", row -> row.lastPaidAt() == null ? "—"
                : row.lastPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
    }

    private void loadPeriods() {
        try {
            List<BillingPeriod> periods = services.billing().list(user);
            periodIds.clear();
            periodFilter.getItems().clear();
            for (BillingPeriod period : periods) {
                periodFilter.getItems().add(period.label());
                periodIds.put(period.label(), period.id());
            }
            if (periods.isEmpty()) {
                footer.setText("Chưa có kỳ thu để theo dõi.");
                return;
            }
            periodFilter.setValue(periods.getFirst().label());
            load();
        } catch (SQLException exception) {
            Ui.error("Không tải được kỳ thu", exception);
        }
    }

    private void load() {
        Long periodId = periodIds.get(periodFilter.getValue());
        if (periodId == null) return;
        try {
            List<HouseholdDebt> debts = services.collections().debts(user, periodId,
                    floorValue(floorFrom), floorValue(floorTo), selectedStatus()).stream()
                    .filter(row -> !"Còn nợ".equals(statusFilter.getValue()) || row.balance().signum() > 0)
                    .toList();
            table.getItems().setAll(debts);
            updateSummary(debts);
        } catch (Exception exception) {
            Ui.error("Không tải được công nợ", exception);
        }
    }

    private void updateSummary(List<HouseholdDebt> debts) {
        BigDecimal totalDue = sum(debts, HouseholdDebt::totalDue);
        BigDecimal paid = sum(debts, HouseholdDebt::paid);
        BigDecimal balance = sum(debts, HouseholdDebt::balance);
        long complete = debts.stream().filter(row -> row.status() == ChargeStatus.PAID).count();
        BigDecimal rate = debts.isEmpty() ? BigDecimal.ZERO : totalDue.signum() == 0 ? new BigDecimal("100")
                : paid.multiply(BigDecimal.valueOf(100)).divide(totalDue, 1, RoundingMode.HALF_UP);
        setMetric(0, "Tổng phải thu", Ui.money(totalDue), "");
        setMetric(1, "Đã thanh toán", Ui.money(paid) + " · " + rate.toPlainString() + "%", "summary-accent");
        setMetric(2, "Còn nợ", Ui.money(balance), "summary-warn");
        setMetric(3, "Hộ đã hoàn tất", complete + "/" + debts.size(), "");
        footer.setText(debts.stream().filter(row -> row.balance().signum() > 0).count()
                + " hộ còn nợ · Tổng số dư " + Ui.money(balance));
    }

    private void setMetric(int index, String title, String value, String valueStyle) {
        summaries.getChildren().set(index, metric(title, value, valueStyle));
        HBox.setHgrow(summaries.getChildren().get(index), Priority.ALWAYS);
    }

    private ChargeStatus selectedStatus() {
        return switch (statusFilter.getValue()) {
            case "Chưa đóng" -> ChargeStatus.UNPAID;
            case "Đóng thiếu" -> ChargeStatus.PARTIALLY_PAID;
            case "Đã đóng đủ" -> ChargeStatus.PAID;
            default -> null;
        };
    }

    private static BigDecimal sum(List<HouseholdDebt> rows, Function<HouseholdDebt, BigDecimal> value) {
        return rows.stream().map(value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static VBox metric(String title, String value, String valueStyle) {
        Label label = Ui.label(title, "summary-label");
        Label amount = Ui.label(value, "summary-value");
        if (!valueStyle.isBlank()) amount.getStyleClass().add(valueStyle);
        VBox box = new VBox(10, label, amount);
        box.getStyleClass().add("summary-item");
        return box;
    }

    private static ComboBox<String> floorPicker() {
        ComboBox<String> floor = new ComboBox<>();
        floor.getItems().add("Tất cả tầng");
        floor.getItems().addAll(java.util.stream.IntStream.rangeClosed(1, 30)
                .mapToObj(value -> "Tầng " + value).toList());
        floor.setValue("Tất cả tầng");
        floor.getStyleClass().add("form-field");
        return floor;
    }

    private static Integer floorValue(ComboBox<String> picker) {
        String value = picker.getValue();
        return value == null || value.equals("Tất cả tầng") ? null : Integer.valueOf(value.substring(5).trim());
    }

    private static String status(ChargeStatus status) {
        return switch (status) {
            case UNPAID -> "Chưa đóng";
            case PARTIALLY_PAID -> "Đóng thiếu";
            case PAID -> "Đã đóng đủ";
        };
    }

    private static TableColumn<HouseholdDebt, String> column(String title,
                                                              Function<HouseholdDebt, String> value) {
        TableColumn<HouseholdDebt, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
