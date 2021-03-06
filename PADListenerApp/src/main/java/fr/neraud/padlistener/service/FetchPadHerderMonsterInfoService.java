package fr.neraud.padlistener.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.neraud.log.MyLog;
import fr.neraud.padlistener.helper.TechnicalSharedPreferencesHelper;
import fr.neraud.padlistener.http.client.RestClient;
import fr.neraud.padlistener.http.exception.ParsingException;
import fr.neraud.padlistener.http.exception.ProcessException;
import fr.neraud.padlistener.http.helper.PadHerderDescriptor;
import fr.neraud.padlistener.http.model.MyHttpRequest;
import fr.neraud.padlistener.http.parser.padherder.MonsterEvolutionJsonParser;
import fr.neraud.padlistener.http.parser.padherder.MonsterInfoJsonParser;
import fr.neraud.padlistener.model.MonsterInfoModel;
import fr.neraud.padlistener.service.helper.UpdateMonsterInfoHelper;

/**
 * Service used to refresh MonsterInfo from PADherder
 *
 * @author Neraud
 */
public class FetchPadHerderMonsterInfoService extends AbstractRestIntentService<Integer> {

	private class FetchInfoTask extends RestTask<List<MonsterInfoModel>> {

		@Override
		protected RestClient createRestClient() {
			return new RestClient(getApplicationContext(), PadHerderDescriptor.serverUrl);
		}

		@Override
		protected MyHttpRequest createMyHttpRequest() {
			return PadHerderDescriptor.RequestHelper.initRequestForGetMonsterInfo();
		}

		@Override
		protected List<MonsterInfoModel> parseResult(String responseContent) throws ParsingException {
			MyLog.entry();
			final MonsterInfoJsonParser parser = new MonsterInfoJsonParser();
			final List<MonsterInfoModel> result = parser.parse(responseContent);
			MyLog.exit();
			return result;
		}
	}

	private class FetchEvolutionsTask extends RestTask<Map<Integer, Integer>> {

		@Override
		protected RestClient createRestClient() {
			return new RestClient(getApplicationContext(), PadHerderDescriptor.serverUrl);
		}

		@Override
		protected MyHttpRequest createMyHttpRequest() {
			return PadHerderDescriptor.RequestHelper.initRequestForGetMonsterEvolution();
		}

		@Override
		protected Map<Integer, Integer> parseResult(String responseContent) throws ParsingException {
			MyLog.entry();
			final MonsterEvolutionJsonParser parser = new MonsterEvolutionJsonParser();
			final Map<Integer, Integer> result = parser.parse(responseContent);
			MyLog.exit();
			return result;
		}
	}

	public FetchPadHerderMonsterInfoService() {
		super("FetchPadHerderMonsterInfoService");
	}

	protected List<RestTask<?>> createRestTasks() {
		final List<RestTask<?>> tasks = new ArrayList<RestTask<?>>();
		tasks.add(new FetchInfoTask());
		tasks.add(new FetchEvolutionsTask());
		return tasks;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Integer processResult(List results) throws ProcessException {
		MyLog.entry();
		final List<MonsterInfoModel> monsters = (List<MonsterInfoModel>) results.get(0);
		final Map<Integer, Integer> evolutions = (Map<Integer, Integer>) results.get(1);

		final UpdateMonsterInfoHelper updateHelper = new UpdateMonsterInfoHelper(getApplicationContext());
		int count = updateHelper.mergeAndSaveMonsterInfo(monsters, evolutions);

		new TechnicalSharedPreferencesHelper(getApplicationContext()).setMonsterInfoRefreshDate(new Date());

		MyLog.exit();
		return count;
	}
}
