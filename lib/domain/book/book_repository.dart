import 'package:zenith/domain/book/snapshot.dart';

/// The phone's copy of the book. Drift implements this in core.
abstract class BookRepository {
  Future<BookSnapshot> load();

  Future<void> save(BookSnapshot book);
}
