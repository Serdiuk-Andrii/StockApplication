package Server;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import Server.Product;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductItemView extends RecursiveTreeObject<ProductItemView> {

    private Product product;

    public ProductItemView(Product product) {
        this.product = product;
    }
}
