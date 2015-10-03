package ex_tep.minhasseries.tratamentos;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ex_tep.minhasseries.entidades.Episodio;
import ex_tep.minhasseries.entidades.Serie;
import ex_tep.minhasseries.entidades.Temporada;
import ex_tep.minhasseries.entidades.Usuario;
import io.realm.RealmList;

import static ex_tep.minhasseries.Constantes.ANO;
import static ex_tep.minhasseries.Constantes.DIA;
import static ex_tep.minhasseries.Constantes.EMISSORA;
import static ex_tep.minhasseries.Constantes.HORARIO;
import static ex_tep.minhasseries.Constantes.ID;
import static ex_tep.minhasseries.Constantes.LOGIN;
import static ex_tep.minhasseries.Constantes.NOME;
import static ex_tep.minhasseries.Constantes.NOTA;
import static ex_tep.minhasseries.Constantes.NUMERO;
import static ex_tep.minhasseries.Constantes.RESUMO;
import static ex_tep.minhasseries.Constantes.SENHA;
import static ex_tep.minhasseries.Constantes.SER_ID;
import static ex_tep.minhasseries.Constantes.TEMP_ID;
import static ex_tep.minhasseries.Constantes.TITULO;
import static ex_tep.minhasseries.Constantes.URL_BUSCAR;
import static ex_tep.minhasseries.Constantes.URL_EDITAR;
import static ex_tep.minhasseries.Constantes.URL_EPISODIO;
import static ex_tep.minhasseries.Constantes.URL_FAVORITO;
import static ex_tep.minhasseries.Constantes.URL_SALVAR;
import static ex_tep.minhasseries.Constantes.URL_SERIE;
import static ex_tep.minhasseries.Constantes.URL_TEMPORADA;
import static ex_tep.minhasseries.Constantes.URL_USUARIO;
import static ex_tep.minhasseries.Constantes.URL_WEBSERVICE;
import static ex_tep.minhasseries.Constantes.USU_ID;

public class TratamentoJSON {

    private TratamentoJSON (){}

    public static void cadastrar(Usuario u) {

        String url = URL_WEBSERVICE + URL_USUARIO + URL_SALVAR;

        List<NameValuePair> parametros = new ArrayList();
        parametros.add(new BasicNameValuePair(NOME, u.getNome()));
        parametros.add(new BasicNameValuePair(LOGIN, u.getLogin()));
        parametros.add(new BasicNameValuePair(SENHA, u.getSenha()));

        TratamentoWebService.makeServiceCall(url, TratamentoWebService.POST, parametros, TratamentoWebService.JSON);
    }

    public static boolean efetuarLogin(String loginUsuario, String senhaUsuario) {

        JSONArray usuarioJSON = buscarJSONArray(URL_USUARIO);
        try {
            for (int i = 0; i < usuarioJSON.length(); i++) {

                JSONObject jsonObject = usuarioJSON.getJSONObject(i);

                String login = jsonObject.getString(LOGIN);
                String senha = jsonObject.getString(SENHA);

                if (login.equals(loginUsuario) && senha.equals(senhaUsuario)) {

                    int id = jsonObject.getInt(ID);
                    String nome = jsonObject.getString(NOME);

                    TratamentoBanco.alterar(new Usuario(id, nome, login, senha, false));
                    return true;
                }
            }

        } catch (JSONException jsonExp) {
            jsonExp.printStackTrace();
        }
        return false;
    }

    public static void atualizarSeries() {

    }

    public static void buscarSeries() {

        JSONArray seriesJSON = buscarJSONArray(URL_SERIE);
        JSONArray idsJSON = buscarJSONArray(URL_FAVORITO);

        List<Integer> ids = getIdFavoritos(idsJSON);
        List<Serie> series = new ArrayList<>();

        try {

            for (int i = 0; i < seriesJSON.length(); i++) {

                JSONObject jObject = seriesJSON.getJSONObject(i);
                int idSerie = jObject.getInt(ID);

                Serie serie = new Serie();

                serie.setId(idSerie);
                serie.setTitulo(jObject.getString(TITULO));
                serie.setNota(Float.parseFloat(jObject.getString(NOTA)));
                serie.setResumo(jObject.getString(RESUMO));
                serie.setEmissora(jObject.getString(EMISSORA));
                serie.setAno(jObject.getInt(ANO));
                serie.setFavorito(false);
                serie.setNotaAlterada(false);
                serie.setMarcado(false);
                serie.setTemporadas(converteJsonTemporadas(idSerie));

                if (ids.contains(serie.getId())) {
                    serie.setFavorito(true);
                }

                series.add(serie);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TratamentoBanco.salvar(series);
    }

    public static void atualizarWebService(Class cls, List<Integer> lista) {

       if (Serie.class.equals (cls)){

            Log.e("atualizarWebService", "Serie - SIZE: " + lista.size());
            for(int i = 0; i < lista.size(); i++){
                int id = lista.get(i);
                Serie s = (Serie) TratamentoBanco.buscar(cls, id);
                enviarDadosWebService(s);
            }

       } else if (Temporada.class.equals(cls)){

            Log.e("atualizarWebService", "TEMP - SIZE: " + lista.size());
            for(int i = 0; i < lista.size(); i++){
                int id = lista.get(i);
                Temporada t = (Temporada) TratamentoBanco.buscar(cls, id);
                enviarDadosWebService(t);
            }

       } else if (Episodio.class.equals (cls)){

            Log.e("atualizarWebService", "EPI - SIZE: " + lista.size());
            for(int i = 0; i < lista.size(); i++){
                int id = lista.get(i);
                Episodio e = (Episodio) TratamentoBanco.buscar(cls, id);
                enviarDadosWebService(e);
            }
       }
    }

    private static RealmList<Temporada> converteJsonTemporadas(int idSerie) throws JSONException {

        JSONArray temporadasJSON = buscarJSONArray(URL_TEMPORADA);
        RealmList<Temporada> temporadas = new RealmList<>();

        for (int i = 0; i < temporadasJSON.length(); i++) {

            JSONObject jObject = temporadasJSON.getJSONObject(i);

            if (idSerie == jObject.getInt(SER_ID)) {

                Temporada temporada = new Temporada();
                int idTemporada = jObject.getInt(ID);

                temporada.setId(idTemporada);
                temporada.setNumero(jObject.getInt(NUMERO));
                temporada.setNota(Float.parseFloat(jObject.getString(NOTA)));
                temporada.setResumo(jObject.getString(RESUMO));
                temporada.setAno(jObject.getInt(ANO));
                temporada.setSerId(idSerie);
                temporada.setNotaAlterada(false);
                temporada.setEpisodios(converteJsonEpisodios(idTemporada));

                temporadas.add(temporada);
            }
        }
        return temporadas;
    }

    private static RealmList<Episodio> converteJsonEpisodios(int idTemporada) throws JSONException {

        JSONArray episodiosJSON = buscarJSONArray(URL_EPISODIO);
        RealmList<Episodio> episodios = new RealmList<>();

        for (int i = 0; i < episodiosJSON.length(); i++) {

            JSONObject jsonObject = episodiosJSON.getJSONObject(i);

            if (idTemporada == jsonObject.getInt(TEMP_ID)) {

                Episodio episodio = new Episodio();
                episodio.setId(jsonObject.getInt(ID));
                episodio.setTitulo(jsonObject.getString(TITULO));
                episodio.setNumero(jsonObject.getInt(NUMERO));
                episodio.setNota(Float.parseFloat(jsonObject.getString(NOTA)));
                episodio.setResumo(jsonObject.getString(RESUMO));
                episodio.setDia(jsonObject.getString(DIA));
                episodio.setHorario(jsonObject.getString(HORARIO));
                episodio.setTempId(idTemporada);
                episodio.setAssistido(false);
                episodio.setNotaAlterada(false);

                episodios.add(episodio);
            }
        }
        return episodios;
    }

    private static List<Integer> getIdFavoritos(JSONArray jsonArray) {

        List<Integer> ids = new ArrayList<>();

        int id = ((Usuario) TratamentoBanco.buscar(Usuario.class)).getId();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = jsonArray.getJSONObject(i);
                if (object.getInt(USU_ID) == id) {
                    ids.add(object.getInt(SER_ID));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ids;
    }

    private static JSONArray buscarJSONArray(String url) {

        try {

            String caminho = URL_WEBSERVICE + url + URL_BUSCAR;
            String json = TratamentoWebService.makeServiceCall(caminho, TratamentoWebService.GET);
            return new JSONArray(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void enviarDadosWebService(Object obj){

        String url = URL_WEBSERVICE;
        List<NameValuePair> parametros = new ArrayList<>();

        if (obj instanceof Episodio){

            Episodio e = (Episodio) obj;

            parametros.add(new BasicNameValuePair(ID, e.getId() + ""));
            parametros.add(new BasicNameValuePair(NUMERO, e.getNumero() + ""));
            parametros.add(new BasicNameValuePair(TITULO, e.getTitulo()));
            parametros.add(new BasicNameValuePair(NOTA, e.getNota() + ""));
            parametros.add(new BasicNameValuePair(RESUMO, e.getResumo()));
            parametros.add(new BasicNameValuePair(DIA, e.getDia()));
            parametros.add(new BasicNameValuePair(HORARIO, e.getHorario()));
            parametros.add(new BasicNameValuePair(TEMP_ID, e.getTempId() + ""));

            url += URL_EPISODIO + URL_EDITAR + e.getId();

        } else if (obj instanceof Temporada){

            Temporada t = (Temporada) obj;

            parametros.add(new BasicNameValuePair(ID, t.getId() + ""));
            parametros.add(new BasicNameValuePair(NUMERO, t.getNumero() + ""));
            parametros.add(new BasicNameValuePair(NOTA, t.getNota() + ""));
            parametros.add(new BasicNameValuePair(RESUMO, t.getResumo()));
            parametros.add(new BasicNameValuePair(ANO, t.getAno() + ""));
            parametros.add(new BasicNameValuePair(SER_ID, t.getSerId() + ""));

            url += URL_TEMPORADA + URL_EDITAR + t.getId();

        } else if (obj instanceof Serie){

            Serie s = (Serie) obj;

            parametros.add(new BasicNameValuePair(ID, s.getId() + ""));
            parametros.add(new BasicNameValuePair(TITULO, s.getTitulo()));
            parametros.add(new BasicNameValuePair(NOTA, s.getNota() + ""));
            parametros.add(new BasicNameValuePair(RESUMO, s.getResumo()));
            parametros.add(new BasicNameValuePair(EMISSORA, s.getEmissora()));
            parametros.add(new BasicNameValuePair(ANO, s.getAno() + ""));

            url += URL_SERIE + URL_EDITAR + s.getId();

        } else if (obj instanceof Usuario){

            Usuario u = (Usuario) obj;

            parametros.add(new BasicNameValuePair(ID, u.getId() + ""));
            parametros.add(new BasicNameValuePair(NOME, u.getNome() + ""));
            parametros.add(new BasicNameValuePair(SENHA, u.getSenha() + ""));
            parametros.add(new BasicNameValuePair(LOGIN, u.getLogin() + ""));

            url += URL_USUARIO + URL_EDITAR + u.getId();
        }

        String log = TratamentoWebService.makeServiceCall(url, TratamentoWebService.PUT, parametros, TratamentoWebService.JSON);
        Log.e("atualizarWebService", "Log: " + log);
    }
}