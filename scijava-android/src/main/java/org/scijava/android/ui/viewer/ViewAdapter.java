package org.scijava.android.ui.viewer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.android.AndroidService;
import org.scijava.android.R;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.widget.WidgetService;

import java.util.ArrayList;
import java.util.List;

public class ViewAdapter<T extends AndroidDataView<?>> extends RecyclerView.Adapter<AndroidViewHolder<?>> implements Contextual {

    @Parameter
    private WidgetService widgetService;

    @Parameter
    private ConvertService convertService;

    @Parameter
    private ObjectService objectService;

    @Parameter
    private AndroidService androidService;

    @Parameter
    private LogService log;

    @Parameter
    private Context context;

    private final ObservableList<T> items;

    private final List<Class<? extends View>> viewTypes;
    private final List<AndroidViewHolderBuilder> viewHolderBuilder;
    private final int adapterLayout;

    public ViewAdapter(Context context, int adapterLayout) {
        setContext(context);
        this.adapterLayout = adapterLayout;
        items = new ObservableArrayList<>();
        items.addOnListChangedCallback(new ObservableListCallback());
        viewTypes = new ArrayList<>();
        viewHolderBuilder = new ArrayList<>();
    }

    public void addItem(T item) {
        items.add(item);
    }

    @Override
    public int getItemViewType(int position) {
        Class<? extends View> componentType = items.get(position).getWidgetType();
        for (int i = 0; i < viewTypes.size(); i++) {
            if (componentType.equals(viewTypes.get(i))) return i;
        }
        viewTypes.add(componentType);

        viewHolderBuilder.add(items.get(position).getViewHolderBuilder());
        return viewTypes.size()-1;
    }
    @NonNull
    @Override
    public AndroidViewHolder<?> onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(androidService.getActivity());
        ViewGroup parentView = (ViewGroup) inflater.inflate(adapterLayout, parent, false);
        ViewGroup contentView = parentView.findViewById(R.id.content);
        return viewHolderBuilder.get(viewType).apply(parentView, contentView);
    }

    @Override
    public void onBindViewHolder(final AndroidViewHolder holder, final int position) {
        T item = items.get(position);
        item.attach(holder);
        holder.input = item;
        TextView textView = holder.labelView;
        if (item.isLabeled()) {
            textView.setText(item.getLabel());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
        holder.input.update();
    }

    @Override
    public void onViewRecycled(@NonNull AndroidViewHolder<?> holder) {
        AndroidDataView item = holder.input;
        if(item != null) {
            item.detach(holder);
            holder.input = null;
        }
        super.onViewRecycled(holder);
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    private void removeItem(T item) {
        int position = items.indexOf(item);
        removeItem(position);
    }

    public void showItem(T item, boolean visible) {
        if(visible) addItem(item);
        else removeItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    private class ObservableListCallback extends  ObservableList.OnListChangedCallback<ObservableList<T>>{

        @Override
        public void onChanged(ObservableList<T> sender) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(ObservableList<T> sender, int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(ObservableList<T> sender, int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(ObservableList<T> sender, int fromPosition, int toPosition, int itemCount) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(ObservableList<T> sender, int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }
    }



}
