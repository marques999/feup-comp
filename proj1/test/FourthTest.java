public class FourthTest
{
    public void test(char a, char b, char c) {
        a++; b++; c++;
        String str = null;
        str.charAt(0);
        test2(str + a + b + c);
    }

    public void test2(String b) {
        b.charAt(0);
        char res = b.charAt(0);
    }
}