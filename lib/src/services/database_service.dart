import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import '../models/equipment.dart';

class DatabaseService {
  DatabaseService._();

  static final DatabaseService instance = DatabaseService._();
  Database? _database;

  Future<Database> get database async {
    if (_database != null) return _database!;

    final path = join(await getDatabasesPath(), 'leitor_claro.db');
    _database = await openDatabase(
      path,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE equipments(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            barcode TEXT NOT NULL,
            equipmentType TEXT NOT NULL,
            equipmentName TEXT NOT NULL,
            contract TEXT NOT NULL,
            capturedAt TEXT NOT NULL,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL,
            street TEXT NOT NULL,
            neighborhood TEXT NOT NULL,
            postalCode TEXT NOT NULL
          )
        ''');
      },
    );

    return _database!;
  }

  Future<void> insertEquipment(Equipment equipment) async {
    final db = await database;
    await db.insert('equipments', equipment.toMap());
  }

  Future<List<Equipment>> fetchEquipments() async {
    final db = await database;
    final rows = await db.query('equipments', orderBy: 'id DESC');
    return rows.map(Equipment.fromMap).toList();
  }
}
