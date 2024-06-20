package com.computech.compras;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;

    public ProductRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, product.getName());
        values.put(DatabaseHelper.COLUMN_QUANTITY, product.getQuantity());
        values.put(DatabaseHelper.COLUMN_PRICE, product.getPrice());

        database.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);
    }

    public void updateProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, product.getName());
        values.put(DatabaseHelper.COLUMN_QUANTITY, product.getQuantity());
        values.put(DatabaseHelper.COLUMN_PRICE, product.getPrice());

        database.update(DatabaseHelper.TABLE_PRODUCTS, values, DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(product.getId())});
    }

    public void deleteProduct(int productId) {
        database.delete(DatabaseHelper.TABLE_PRODUCTS, DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(productId)});
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY));
                @SuppressLint("Range") double price = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRICE));

                products.add(new Product(id, name, quantity, price));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return products;
    }
}
