package Server;

import Server.Group;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupItemView extends RecursiveTreeObject<GroupItemView> {

    private Group group;

    public GroupItemView(Group group) {
        this.group = group;
    }
}
