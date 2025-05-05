package foodies.io;

import foodies.entity.OrderItemEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderRequest {
    private List<OrderItemEntity> orderedItems;
    private String userAddress;
    private double amount;
    private String email;
    private String phoneNumber;
    private String orderStatus;

}
