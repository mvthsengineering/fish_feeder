package root.fishfeeder;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by student on 1/12/2018.
 */

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ProductViewHolder> {

    private Context mCtx;

    private CardView ll;

    private List<FeedLog> productList;

    public LogAdapter(Context mCtx, List<FeedLog> productList, CardView ll) {
        this.mCtx = mCtx;
        this.ll = ll;
        this.productList = productList;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.log, ll);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        FeedLog product = productList.get(position);
        holder.logtv.setText(product.getLog());
        holder.datetv.setText(product.getDate());
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }


    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView datetv, logtv;

        public ProductViewHolder(View itemView) {
            super(itemView);

            logtv = itemView.findViewById(R.id.log);
            datetv = itemView.findViewById(R.id.date);
        }
    }
}