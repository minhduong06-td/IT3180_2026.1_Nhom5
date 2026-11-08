package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.FeeType;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

final class FeeView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final TableView<FeeType> table = new TableView<>();

    FeeView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        var add = Ui.button("Thêm khoản thu", true);
        add.setOnAction(event -> createFee());
        var addRate = Ui.button("Cập nhật đơn giá", false);
        addRate.setOnAction(event -> addRate());
        var refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        getChildren().addAll(Ui.pageHeader("Khoản thu", "Đơn giá mới chỉ áp dụng cho kỳ được phát hành sau ngày hiệu lực."),
                Ui.toolbar(Ui.label("Khoản đã phát hành luôn giữ nguyên diện tích và đơn giá tại thời điểm tạo.", "muted"),
                        Ui.spacer(), refresh, addRate, add), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Tên khoản thu", FeeType::name));
        table.getColumns().add(column("Mã", FeeType::code));
        table.getColumns().add(column("Cách tính", fee -> fee.basis() == CalculationBasis.PER_M2 ? "Theo diện tích" : "Cố định"));
        table.getColumns().add(column("Bắt buộc", fee -> fee.required() ? "Có" : "Không"));
        table.getColumns().add(column("Đơn giá gần nhất", fee -> fee.unitPrice() == null
                ? "Chưa có đơn giá hiện hành" : Ui.money(fee.unitPrice())));
        table.getColumns().add(column("Hiệu lực từ", fee -> Ui.date(fee.effectiveFrom())));
        table.getColumns().add(column("Trạng thái", fee -> fee.active() ? "Đang áp dụng" : "Ngừng áp dụng"));
    }

    private void load() {
        try {
            table.getItems().setAll(services.fees().list(user));
        } catch (SQLException exception) {
            Ui.error("Không tải được khoản thu", exception);
        }
    }

    private void createFee() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm khoản thu");
        dialog.setHeaderText("Thông tin và đơn giá khởi tạo");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Tạo khoản thu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        var code = Ui.textField("Ví dụ: SERVICE");
        var name = Ui.textField("Tên khoản thu");
        var price = Ui.textField("Ví dụ: 3000");
        var basis = basisPicker();
        var effective = new DatePicker(LocalDate.now());
        effective.getStyleClass().add("form-field");
        GridPane form = form();
        addRow(form, 0, "Mã", code);
        addRow(form, 1, "Tên khoản thu", name);
        addRow(form, 2, "Cách tính", basis);
        addRow(form, 3, "Đơn giá", price);
        addRow(form, 4, "Hiệu lực từ", effective);
        form.add(new Label("Khoản thu bắt buộc hàng tháng."), 1, 5);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(500);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                services.fees().create(user, code.getText(), name.getText(), basis.getValue(),
                        decimal(price.getText()), effective.getValue());
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không tạo được khoản thu", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private void addRate() {
        FeeType fee = table.getSelectionModel().getSelectedItem();
        if (fee == null) {
            Ui.info("Chọn khoản thu", "Chọn khoản thu cần cập nhật đơn giá.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật đơn giá");
        dialog.setHeaderText(fee.name());
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu đơn giá", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        var price = Ui.textField("Ví dụ: 3500");
        var effective = new DatePicker(LocalDate.now());
        effective.getStyleClass().add("form-field");
        GridPane form = form();
        addRow(form, 0, "Đơn giá mới", price);
        addRow(form, 1, "Hiệu lực từ", effective);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(460);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                services.fees().addRate(user, fee.id(), decimal(price.getText()), effective.getValue());
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không lưu được đơn giá", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private static ComboBox<CalculationBasis> basisPicker() {
        ComboBox<CalculationBasis> basis = new ComboBox<>();
        basis.getItems().setAll(CalculationBasis.values());
        basis.setValue(CalculationBasis.PER_M2);
        basis.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CalculationBasis value) {
                return value == CalculationBasis.PER_M2 ? "Theo diện tích (m² × đơn giá)" : "Mức cố định";
            }
            @Override public CalculationBasis fromString(String value) { return CalculationBasis.PER_M2; }
        });
        basis.getStyleClass().add("form-field");
        return basis;
    }

    private static GridPane form() {
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        return form;
    }

    private static void addRow(GridPane form, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        form.add(label, 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value.trim().replace(',', '.'));
    }

    private static TableColumn<FeeType, String> column(String title, Function<FeeType, String> value) {
        TableColumn<FeeType, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
