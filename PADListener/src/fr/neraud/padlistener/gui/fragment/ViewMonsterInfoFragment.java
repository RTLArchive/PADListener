
package fr.neraud.padlistener.gui.fragment;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import fr.neraud.padlistener.R;
import fr.neraud.padlistener.gui.helper.MonsterInfoCursorAdapter;
import fr.neraud.padlistener.provider.descriptor.MonsterInfoDescriptor;

public class ViewMonsterInfoFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(getClass().getName(), "onCreate");

		adapter = new MonsterInfoCursorAdapter(getActivity(), R.layout.view_monster_info_item);
		setListAdapter(adapter);

		getLoaderManager().initLoader(0, null, this);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(getClass().getName(), "onCreateLoader");
		return new CursorLoader(getActivity(), MonsterInfoDescriptor.UriHelper.uriForAll(), null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(getClass().getName(), "onLoadFinished");
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(getClass().getName(), "onLoaderReset");
		adapter.swapCursor(null);
	}
}
