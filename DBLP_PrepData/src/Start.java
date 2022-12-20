public class Start {

    private static void path(Person_ p1, Person_ p2) {
        CoauthorPath cp = new CoauthorPath(p1,p2);
        Person_ path[] = cp.getPath();
        int i;
        System.out.println("# of Person objects = " + Person_.numberOfPersons());
        if (path == null) {
            System.out.println("<"+p1+","+p2+"> not connected");
            return;
        }

        for (i=0; i<path.length; i++) {
            System.out.println(i + ": " + path[i] + " " +
                    path[i].getNumberOfPublications() + " " +
                    (path[i].getPersonRecord()==null?" ":"*"));
        }
        System.out.println();
    }


    public static void main(String[] args) {
        Person_ p1, p2;
        p1 = Person_.create("Michael Ley", "l/Ley:Michael");
        p2 = Person_.create("Peter Sturm", "s/Sturm:Peter");
        path(p1,p2);
        p2 = Person_.create("Bernd Walter", "w/Walter:Bernd");
        path(p1,p2);
        p2 = Person_.create("Alan T. Murray", "m/Murray:Alan_T=");
        path(p1,p2);
        p2 = Person_.create("B. Vogel", "v/Vogel:B=");
        path(p1,p2);
        p2 = Person_.create("Alan M. Turing", "t/Turing:Alan_M=");
        path(p1,p2);
    }

}