import com.google.api.services.classroom.Classroom;
import com.google.api.services.admin.directory.Directory;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.google.api.services.classroom.model.*;
import com.google.api.services.admin.directory.model.*;

public class Main {

    private static String objectToJson(Object objeto) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(objeto);
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        Classroom servicioClassroom = ClassroomJavaAPI.obtenerServicio();
        Directory servicioUsuario = GsuiteJavaAPI.obtenerServicio();

        //TODO: Hacer demo con las clases

    }
}
