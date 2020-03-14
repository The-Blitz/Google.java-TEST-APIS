import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.*;

import javafx.util.Pair;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GsuiteJavaAPI {
    private static final String APPLICATION_NAME = "Google Admin SDK Directory API Java";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "gsuite-tokens";

    /**
     * Los SCOPES son los permisos que se le da al servicio, dependiendo de lo que se necesite hacer.
     * Recuerde borrar los tokens en caso modifique los SCOPES
     */
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/admin.directory.user"
    );
    private static final String CREDENTIALS_FILE_PATH = "/gsuite-credentials.json";

    /**
     * Se crea un objeto de credencial autorizada
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GsuiteJavaAPI.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    //Se crea un nuevo correo email, con los datos de la persona
    private static User crearUsuario(Directory servicio , String correo, String apellidos, String nombres, List<Pair<Character,String>> listaGrupos) throws IOException{
        UserName nombreUsuario = new UserName();
        nombreUsuario.setGivenName(nombres);
        nombreUsuario.setFamilyName(apellidos);
        nombreUsuario.setFullName(nombres+" "+apellidos);

        User usuario = new User();
        usuario.setName(nombreUsuario);
        usuario.setPassword("Nueva Clave de Prueba"); // TODO: cambiar esto por un Hash o algo mas seguro
        usuario.setPrimaryEmail(correo);

        Character parametro = correo.charAt(0);

        for(int i=0; i<listaGrupos.size() ; i++){
            Character tipo= listaGrupos.get(i).getKey();
            String grupo = listaGrupos.get(i).getValue();
            if(parametro == tipo) {
                usuario.setOrgUnitPath(grupo);
            }
        }

        usuario = servicio.users().insert(usuario).execute();
        System.out.println(java.text.MessageFormat.format( "{0} con correo {1} fue creado con id {2}" , usuario.getName().getFullName(), usuario.getPrimaryEmail(), usuario.getId() ));

        return usuario;
    }

    //TODO: Agregar excepcion
    //Se elimina el correo en caso exista
    private static void borrarUsuario(Directory servicio, String correo) throws IOException{
        servicio.users().delete(correo).execute();
        System.out.println(java.text.MessageFormat.format( "Usuario con correo {0} fue eliminado" , correo ));
    }

    //Obtener al Usuario de acuerdo a su ID
    private static User obtenerUsuarioporID(Directory servicio, String usuarioId) throws IOException{
        return servicio.users().get(usuarioId).execute();
    }

    //Imprime los primeros usuarios(depende de la cantidad) a las que se tienen accesso
    private static void listarUsuarios(Directory servicio, Integer cantidadCorreos) throws IOException{
        Users result = servicio.users().list().setCustomer("my_customer").setMaxResults(cantidadCorreos).setOrderBy("email").execute();
        List<User> usuarios = result.getUsers();
        if (usuarios == null || usuarios.size() == 0) {
            System.out.println("No se encontraron usuarios");
        } else {
            System.out.println("Usuarios:");
            for (User usuario : usuarios) {
                System.out.println(usuario.getName().getFullName());
            }
        }
    }

    public static Directory obtenerServicio() throws IOException,GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Directory servicio = obtenerServicio();

    }
}