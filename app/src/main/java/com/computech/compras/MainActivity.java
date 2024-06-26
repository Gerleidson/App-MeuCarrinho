package com.computech.compras;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText productNameEditText, productQuantityEditText, productPriceEditText;
    private TextView totalTextView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private double totalAmount = 0;
    private int editingPosition = -1;
    private ProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productNameEditText = findViewById(R.id.editTextText);
        productQuantityEditText = findViewById(R.id.editTextText2);
        productPriceEditText = findViewById(R.id.editTextText3);
        ImageButton addButton = findViewById(R.id.imageButton);
        ImageButton clearButton = findViewById(R.id.imageButton2);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        totalTextView = findViewById(R.id.totalTextView);

        productRepository = new ProductRepository(this);
        productRepository.open();

        productList = productRepository.getAllProducts();
        productAdapter = new ProductAdapter(productList, new ProductAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                editProduct(position);
            }

            @Override
            public void onDeleteClick(int position) {
                deleteProduct(position);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

        addButton.setOnClickListener(v -> addProduct());

        clearButton.setOnClickListener(v -> clearAll());

        int space = getResources().getDimensionPixelSize(R.dimen.item_space);
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));

        updateTotalAmount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        productRepository.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemOrdenar) {
            sortListAlphabetically(); // Chama a função para ordenar a lista
            Toast.makeText(MainActivity.this, "Lista Ordenada", Toast.LENGTH_LONG).show();
        } else if (id == R.id.itemCompartilharApp) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Compartilhar App");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Veja este aplicativo incrível: https://drive.google.com/file/d/19B-0q0SGSVBsKiC2NyicbdSDaE40IBd4/view?usp=sharing");
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
        }  else if (id == R.id.itemCompartilharLista) {
            shareListViaWhatsApp(); // Chama a função para compartilhar a lista via WhatsApp
        }
        else if (id == R.id.itemSobre) {
            showAboutDialog();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Meu Carrinho");
        builder.setMessage(
                "Versão: 1.0\n" +
                        "Desenvolvedor: Gerleidson A. Bomfim\n" +
                        "Contato: gerleidson.bomfim@gmail.com\n" +
                        "Obrigado por usar o aplicativo.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void shareListViaWhatsApp() {
        StringBuilder message = new StringBuilder("Minha lista de compras:\n");
        for (Product product : productList) {
            message.append("- ").append(product.getName()).append(": ").append(product.getQuantity()).append(" unidades\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        shareIntent.setPackage("com.whatsapp"); // Define o pacote do WhatsApp

        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "WhatsApp não está instalado.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
    private void addProduct() {
        String name = productNameEditText.getText().toString();
        String quantityString = productQuantityEditText.getText().toString();
        String priceString = productPriceEditText.getText().toString();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(quantityString) && !TextUtils.isEmpty(priceString)) {
            int quantity = Integer.parseInt(quantityString);
            double price = Double.parseDouble(priceString);
            double total = quantity * price;

            Product product = new Product(name, quantity, price);

            if (editingPosition >= 0) {
                // Atualiza o produto na posição de edição
                Product previousProduct = productList.get(editingPosition);
                double previousTotal = previousProduct.getQuantity() * previousProduct.getPrice();

                product.setId(previousProduct.getId());
                productRepository.updateProduct(product);

                productList.set(editingPosition, product);
                totalAmount = totalAmount - previousTotal + total; // Atualiza o valor total
                editingPosition = -1;
            } else {
                // Adiciona um novo produto
                productRepository.addProduct(product);
                productList.add(product);
                totalAmount += total; // Atualiza o valor total
            }
            productAdapter.notifyDataSetChanged();

            totalTextView.setText(String.format("R$ %.2f", totalAmount));

            productNameEditText.setText("");
            productQuantityEditText.setText("");
            productPriceEditText.setText("");
        }
    }

    @SuppressLint("DefaultLocale")
    private void deleteProduct(int position) {
        Product product = productList.get(position);

        // Cria um AlertDialog para confirmar a exclusão
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar exclusão");
        builder.setMessage("Tem certeza que deseja excluir este item?");
        builder.setPositiveButton("Sim", (dialog, which) -> {
            double total = product.getQuantity() * product.getPrice();

            productRepository.deleteProduct(product.getId());

            totalAmount -= total; // Atualiza o valor total
            totalTextView.setText(String.format("R$ %.2f", totalAmount));

            productList.remove(position);
            productAdapter.notifyItemRemoved(position);
        });
        builder.setNegativeButton("Não", null); // Botão de cancelar

        // Mostra o AlertDialog
        builder.show();
    }

    @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
    private void clearAll() {
        productList.clear();
        totalAmount = 0;
        totalTextView.setText(String.format("R$ %.2f", totalAmount));
        productAdapter.notifyDataSetChanged();
    }

    private void editProduct(int position) {
        Product product = productList.get(position);
        productNameEditText.setText(product.getName());
        productQuantityEditText.setText(String.valueOf(product.getQuantity()));
        productPriceEditText.setText(String.valueOf(product.getPrice()));

        // Armazena a posição de edição do produto
        editingPosition = position;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortListAlphabetically() {
        Collections.sort(productList, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));

        // Atualiza o adaptador da RecyclerView
        productAdapter.notifyDataSetChanged();
    }

    @SuppressLint("DefaultLocale")
    private void updateTotalAmount() {
        totalAmount = 0;
        for (Product product : productList) {
            totalAmount += product.getQuantity() * product.getPrice();
        }
        totalTextView.setText(String.format("R$ %.2f", totalAmount));
    }
}
