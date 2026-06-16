package org.example.praktika;

import com.fasterxml.jackson.core.type.TypeReference;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserDAO extends SupabaseDAO {


    public CompletableFuture<List<User>> getAllUsers() {
        HttpRequest request = createBaseRequestBuilder("/users?select=*").GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<User>>(){}, "getAllUsers");
    }
}