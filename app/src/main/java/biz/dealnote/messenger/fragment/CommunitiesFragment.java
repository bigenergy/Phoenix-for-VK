package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.CommunitiesAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.DataWrapper;
import biz.dealnote.messenger.mvp.presenter.CommunitiesPresenter;
import biz.dealnote.messenger.mvp.view.ICommunitiesView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.view.MySearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public class CommunitiesFragment extends BasePresenterFragment<CommunitiesPresenter, ICommunitiesView>
        implements ICommunitiesView, MySearchView.OnQueryTextListener, CommunitiesAdapter.ActionListener {

    public static CommunitiesFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        CommunitiesFragment fragment = new CommunitiesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CommunitiesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_communities, container, false);
        //((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new CommunitiesAdapter(getActivity(), Collections.emptyList(), new int[0]);
        mAdapter.setActionListener(this);

        recyclerView.setAdapter(mAdapter);

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setOnQueryTextListener(this);
        return root;
    }

    @Override
    public void displayData(DataWrapper<Community> own, DataWrapper<Community> filtered, DataWrapper<Community> seacrh) {
        if(nonNull(mAdapter)){
            List<DataWrapper<Community>> wrappers = new ArrayList<>();
            wrappers.add(own);
            wrappers.add(filtered);
            wrappers.add(seacrh);

            int[] titles = {R.string.my_communities_title, R.string.quick_search_title, R.string.other};
            mAdapter.setData(wrappers, titles);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActivityUtils.setToolbarTitle(this, R.string.groups);
        ActivityUtils.setToolbarSubtitle(this, null); // TODO: 04.10.2017

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void notifyDataSetChanged() {
        if(nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyOwnDataAdded(int position, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(0, position, count);
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void showCommunityWall(int accountId, Community community) {
        PlaceFactory.getOwnerWallPlace(accountId, community).tryOpenWith(getActivity());
    }

    @Override
    public IPresenterFactory<CommunitiesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunitiesPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.USER_ID),
                saveInstanceState
        );
    }

    @Override
    protected String tag() {
        return CommunitiesFragment.class.getSimpleName();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getPresenter().fireSearchQueryChanged(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        getPresenter().fireSearchQueryChanged(newText);
        return true;
    }

    @Override
    public void onCommunityClick(Community community) {
        getPresenter().fireCommunityClick(community);
    }
}