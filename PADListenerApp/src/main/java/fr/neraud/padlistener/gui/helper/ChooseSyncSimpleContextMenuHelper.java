package fr.neraud.padlistener.gui.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;

import fr.neraud.padlistener.R;
import fr.neraud.padlistener.model.ChooseSyncModelContainer;
import fr.neraud.padlistener.model.SyncedMonsterModel;
import fr.neraud.padlistener.padherder.constant.MonsterPriority;

/**
 * Helper to build and manage a context menu when displaying monster in a grouped list
 * Created by Neraud on 22/06/2014.
 */
public class ChooseSyncSimpleContextMenuHelper extends ChooseSyncBaseContextMenuHelper {

	private static final int MENU_ID_SELECT = 1;
	private static final int MENU_ID_DESELECT = 2;
	private static final int MENU_ID_CHANGE_PRIORITY = 3;
	private static final int MENU_ID_CHANGE_NOTE = 4;

	private final ChooseSyncMonstersSimpleAdapter adapter;

	public ChooseSyncSimpleContextMenuHelper(Context context, ChooseSyncDataPagerHelper.Mode mode, ChooseSyncMonstersSimpleAdapter adapter) {
		super(context, mode);
		this.adapter = adapter;
	}

	public void createContextMenu(ContextMenu menu, ContextMenu.ContextMenuInfo menuInfo) {
		Log.d(getClass().getName(), "createContextMenu : " + menuInfo);

		final ChooseSyncModelContainer<SyncedMonsterModel> monsterItem = getMonsterItem(menuInfo);
		menu.setHeaderTitle(getContext().getString(R.string.choose_sync_context_menu_one_title, monsterItem.getSyncedModel().getMonsterInfo().getName()));
		if (monsterItem.isChosen()) {
			menu.add(getGroupId(), MENU_ID_DESELECT, 0, R.string.choose_sync_context_menu_one_deselect);
		} else {
			menu.add(getGroupId(), MENU_ID_SELECT, 0, R.string.choose_sync_context_menu_one_select);
		}

		if (getMode() != ChooseSyncDataPagerHelper.Mode.DELETED) {
			menu.add(getGroupId(), MENU_ID_CHANGE_PRIORITY, 0, R.string.choose_sync_context_menu_one_change_priority);
			menu.add(getGroupId(), MENU_ID_CHANGE_NOTE, 0, R.string.choose_sync_context_menu_one_change_note);
		}
	}

	public boolean doContextItemSelected(MenuItem menuItem) {
		Log.d(getClass().getName(), "doContextItemSelected : menuItem = " + menuItem);

		final ChooseSyncModelContainer<SyncedMonsterModel> monsterItem = getMonsterItem(menuItem.getMenuInfo());

		switch (menuItem.getItemId()) {
			case MENU_ID_SELECT:
				monsterItem.setChosen(true);
				notifyDataSetChanged();
				break;
			case MENU_ID_DESELECT:
				monsterItem.setChosen(false);
				notifyDataSetChanged();
				break;
			case MENU_ID_CHANGE_PRIORITY:
				AlertDialog.Builder priorityDialogBuilder = new AlertDialog.Builder(getContext());
				final String priorityDialogTitle = getContext().getString(R.string.choose_sync_context_menu_one_change_priority_dialog_title, monsterItem.getSyncedModel().getMonsterInfo().getName());
				priorityDialogBuilder.setTitle(priorityDialogTitle);
				int selected = monsterItem.getSyncedModel().getCapturedInfo().getPriority().ordinal();
				final CharSequence[] priorities = buildPriorityList();

				priorityDialogBuilder.setSingleChoiceItems(priorities, selected, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Log.d(getClass().getName(), "onClick : i = " + i);
						final MonsterPriority priority = MonsterPriority.findByOrdinal(i);
						monsterItem.getSyncedModel().getCapturedInfo().setPriority(priority);
						dialogInterface.dismiss();
						notifyDataSetChanged();
					}
				});

				priorityDialogBuilder.create().show();
				break;
			case MENU_ID_CHANGE_NOTE:
				AlertDialog.Builder noteDialogBuilder = new AlertDialog.Builder(getContext());
				final String noteDialogTitle = getContext().getString(R.string.choose_sync_context_menu_one_change_note_dialog_title, monsterItem.getSyncedModel().getMonsterInfo().getName());
				noteDialogBuilder.setTitle(noteDialogTitle);

				final EditText input = new EditText(getContext());
				input.setText(monsterItem.getSyncedModel().getCapturedInfo().getNote());
				noteDialogBuilder.setView(input);
				noteDialogBuilder.setPositiveButton(R.string.choose_sync_context_menu_change_note_dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Log.d(getClass().getName(), "onClick");
						String newNote = input.getText().toString().trim();
						monsterItem.getSyncedModel().getCapturedInfo().setNote(newNote);
						notifyDataSetChanged();
					}
				});

				noteDialogBuilder.setNegativeButton(R.string.choose_sync_context_menu_change_note_dialog_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
				noteDialogBuilder.create().show();

				break;
			default:
		}

		return true;
	}


	private ChooseSyncModelContainer<SyncedMonsterModel> getMonsterItem(ContextMenu.ContextMenuInfo menuInfo) {
		Log.d(getClass().getName(), "getMonsterItem : " + menuInfo);
		AdapterView.AdapterContextMenuInfo listItem = (AdapterView.AdapterContextMenuInfo) menuInfo;
		return adapter.getItem(listItem.position);
	}

	protected void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}
}
