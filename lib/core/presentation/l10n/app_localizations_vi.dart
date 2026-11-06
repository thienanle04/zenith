// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Vietnamese (`vi`).
class AppLocalizationsVi extends AppLocalizations {
  AppLocalizationsVi([String locale = 'vi']) : super(locale);

  @override
  String get appTitle => 'Zenith';

  @override
  String get portfolio => 'Danh mục';

  @override
  String get assets => 'Tài sản';

  @override
  String get liabilities => 'Nợ';

  @override
  String get netWorth => 'Tài sản ròng';

  @override
  String get physicalHoldings => 'Hiện vật';

  @override
  String get addAccount => 'Thêm tài khoản';

  @override
  String get addHolding => 'Thêm nắm giữ';

  @override
  String get name => 'Tên';

  @override
  String get kind => 'Loại';

  @override
  String get currency => 'Tiền tệ';

  @override
  String get balance => 'Số dư';

  @override
  String get unit => 'Đơn vị';

  @override
  String get price => 'Giá';

  @override
  String get quantity => 'Số lượng';

  @override
  String get save => 'Lưu';

  @override
  String get setAside => 'Để riêng';

  @override
  String get removeHolding => 'Xóa nắm giữ';

  @override
  String get removeInstrument => 'Xóa công cụ';

  @override
  String get shares => 'Cổ phiếu';

  @override
  String get physical => 'Hiện vật';

  @override
  String get rate => 'Tỷ giá';

  @override
  String get moveUp => 'Lên';

  @override
  String get moveDown => 'Xuống';

  @override
  String get cash => 'Tiền mặt';

  @override
  String get bank => 'Ngân hàng';

  @override
  String get eWallet => 'Ví điện tử';

  @override
  String get creditCard => 'Thẻ tín dụng';

  @override
  String get loan => 'Khoản vay';

  @override
  String get receivable => 'Khoản phải thu';

  @override
  String get stock => 'Tài khoản chứng khoán';

  @override
  String get nameRequired => 'Cần có tên.';

  @override
  String get currencyNeedsRate => 'Tiền tệ này cần một tỷ giá trước.';

  @override
  String get stockKindFixed => 'Tài khoản chứng khoán giữ nguyên loại.';

  @override
  String get stockAccountRequired =>
      'Cổ phiếu thuộc về một tài khoản chứng khoán.';

  @override
  String get ratePositive => 'Tỷ giá phải là số dương.';

  @override
  String get notFound => 'Bản ghi này không còn trong sổ.';

  @override
  String get invalidAmount => 'Hãy nhập một số.';
}
