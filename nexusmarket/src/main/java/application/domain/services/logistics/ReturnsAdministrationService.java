package application.domain.services.logistics;

import application.domain.enums.OrderStatus;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import java.util.Objects;

/**
 * 6.3 Manages reverse logistics workflows (FR-023, FR-024, FR-025).
 */
public class ReturnsAdministrationService {

    public ReturnRequest initiateReturn(Order order) {
        return initiateReturn(order, "Customer requested return");
    }

    public ReturnRequest initiateReturn(Order order, String reason) {
        Objects.requireNonNull(order, "Order must not be null");
        if (order.getStatus() != OrderStatus.DELIVERED_COMPLETED) {
            throw new IllegalStateException("Solo se puede solicitar devolución para órdenes entregadas (DELIVERED_COMPLETED)");
        }
        return ReturnRequest.create(order, reason);
    }

    public void approveReturn(ReturnRequest returnRequest) {
        Objects.requireNonNull(returnRequest, "ReturnRequest must not be null");
        returnRequest.approve();
    }

    public void rejectReturn(ReturnRequest returnRequest) {
        Objects.requireNonNull(returnRequest, "ReturnRequest must not be null");
        returnRequest.reject();
    }

    public void completeReturn(ReturnRequest returnRequest) {
        Objects.requireNonNull(returnRequest, "ReturnRequest must not be null");
        returnRequest.complete();
    }
}
