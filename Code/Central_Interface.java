import java.rmi.*;

public interface Central_Interface extends Remote
{
  int swapMissingPage(int missed_page_number) throws Exception;
}
