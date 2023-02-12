package estg.ipp.pt.sd_grupo6_client.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class SynchronizedArrayList<T> {
    private final List<T> list = new ArrayList<T>();
    private final ObservableList<T> oli = FXCollections.observableList(list);

    public void addListener(ListChangeListener<T> listChangeListener){
        oli.addListener(
                listChangeListener
        );
    }

    public synchronized void add(T o) {
        oli.add(o);
    }

    public synchronized void clearList() {
        oli.clear();
    }

    public List<T> getList(){
        return list;
    }

    public void list(){
        for (T t : oli) {
            System.out.println(t);
        }
    }
}
