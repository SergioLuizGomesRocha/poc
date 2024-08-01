package getStream;

import io.getstream.chat.java.exceptions.StreamException;
import io.getstream.chat.java.models.Channel;
import io.getstream.chat.java.models.Channel.ChannelGetResponse;
import io.getstream.chat.java.models.Channel.ChannelQueryMembersResponse;
import io.getstream.chat.java.models.Channel.ChannelRequestObject;
import io.getstream.chat.java.models.FilterCondition;
import io.getstream.chat.java.models.Message;
import io.getstream.chat.java.models.Message.MessageRequestObject;
import io.getstream.chat.java.models.Sort;
import io.getstream.chat.java.models.Sort.Direction;
import io.getstream.chat.java.models.User;
import io.getstream.chat.java.models.User.UserRequestObject;
import io.getstream.chat.java.models.User.UserUpsertResponse;
import io.getstream.chat.java.services.framework.DefaultClient;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Properties;

public class StreamCreatingChannel {


  private static ChannelGetResponse getChannelId(UserRequestObject user, String idChannel) throws StreamException {
    return Channel.getOrCreate("messaging", idChannel)
        .data(
            ChannelRequestObject.builder()
                .createdBy(user)
                .additionalField("custom_field", "custom_value") // Optional custom fields
                .build())
        .request();
  }

  private static void getListMembers(String idChannel) throws StreamException {
    ChannelQueryMembersResponse members = Channel.queryMembers().
        type("messaging").id(idChannel).sort(Sort.builder().field("created_at").
            direction(Direction.ASC).build()).limit(100).request();
    System.out.println("Channel MEMBERS ========> " + members.getMembers());
  }

  private static void addMemberInChannel(String idChannel, String type,
      String member) throws StreamException {
    Channel.update(type,idChannel).addMember(member).hideHistory(Boolean.TRUE).request();
  }

  private static void getListChannel() throws StreamException {
    User user = new User();
    user.setId("testes-1");
    user.setRole("Admin");
    user.setTeams(Collections.singletonList("Moderation"));
    user.setOnline(Boolean.FALSE);
    user.setInvisible(Boolean.TRUE);

    Channel.list()
        .user(UserRequestObject.buildFrom(user))
        .filterCondition("type", "messaging")
        .filterConditions(FilterCondition.in("members", "testes-1"))
        .sort(Sort.builder().field("last_message_at").direction(Direction.DESC).build())
        .watch(true)
        .state(true)
        .request();

  }

  private static String getToken(){
    var calendar = new GregorianCalendar();
    calendar.add(Calendar.MINUTE, 60);

    var token = User.createToken("13796314", calendar.getTime(), null);
    return token;
  }

  private static UserUpsertResponse syncingUsers(String id, String name) throws StreamException {
    var usersUpsertRequest = User.upsert();
    usersUpsertRequest.user(UserRequestObject.builder().id(id).name(name).build());

    return usersUpsertRequest.request();
  }

  private static UserUpsertResponse createUser(String id, String name, String role) throws StreamException {
    var user = UserRequestObject.builder().id(id).name(name).role(role).build();
    return User.upsert().user(user).request();
  }

  private static void sendMessage(String idChannel, String message, String userId) throws StreamException {

    Message.send("messaging", idChannel)
        .message(
            MessageRequestObject.builder()
                .text(message)
                .userId(userId)
                .build())
        .request();
  }

  public static void main(String [] args) throws StreamException {

    var properties = new Properties();
    properties.put(DefaultClient.API_KEY_PROP_NAME, "g8h822f3a7p9"); //<api-key>
    properties.put(DefaultClient.API_SECRET_PROP_NAME,
        "wmppan4uf6kxasr58a3j2jv22f8gp2anz9d68jt3jsqey6afw3qqrn4jwt3ddk94");//<api-secret>
    var client = new DefaultClient(properties);
    DefaultClient.setInstance(client);

    // Criar um usuário este exemplo é o aluno do curso
    var userConsumer = createUser("13796314","Breno Reis","user");

    //Criar o channel
    ChannelGetResponse channel = getChannelId(
        UserRequestObject.buildFrom(userConsumer.getUsers().get(userConsumer.getUsers())),
        "12055115-13796314");
    System.out.println("Channel ID ========> ".concat(channel.getChannel().getId()));

   // enviar uma mensage para o ownner
    sendMessage(channel.getChannel().getId(), "Mensagem do consumer para owner",
        userConsumer.getUsers().get(0).getId());

    //Criar o usuário este exemplo e o owner do produto
    var userOwner = createUser("12055115","Sergio","admin");

    //Envia uma resposta
    sendMessage(channel.getChannel().getId(), "Mensagem do owner para o consumer",
        userOwner.getUsers().get(0).getId());
  }
}
