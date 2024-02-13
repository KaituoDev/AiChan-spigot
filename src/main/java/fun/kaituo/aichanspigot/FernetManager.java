package fun.kaituo.aichanspigot;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;

public class FernetManager {
    private final Key key;

    public final Validator<String> validator = new StringValidator() {
    };


    @SuppressWarnings("unused")
    public FernetManager(Key key) {
        this.key = key;
    }

    @SuppressWarnings("unused")
    public FernetManager(String keyString){
        this.key = new Key(keyString);
    }

    public String encrypt(String rawMessage) {
        return Token.generate(key, rawMessage).serialise();
    }

    public String decrypt(String encryptedMessage) {
        Token token = Token.fromString(encryptedMessage);
        return token.validateAndDecrypt(key, validator);
    }
}
