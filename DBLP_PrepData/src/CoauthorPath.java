import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class CoauthorPath {
    private Person_ path[];

    public CoauthorPath(Person_ p1, Person_ p2) {
        shortestPath(p1,p2);
    }

    public Person_[] getPath() { return path; }

    private void tracing(int position) {
        Person_ pNow, pNext;
        int direction, i, label;

        label = path[position].getLabel();
        direction = Integer.signum(label);
        label -= direction;
        while (label != 0) {
            pNow = path[position];
            Person_ ca[] = pNow.getCoauthors();
            for (i=0; i<ca.length; i++) {
                pNext = ca[i];
                if (!pNext.hasLabel())
                    continue;
                if (pNext.getLabel() == label) {
                    position -= direction;
                    label -= direction;
                    path[position] = pNext;
                    break;
                }
            }
        }
    }

    private void shortestPath(Person_ p1, Person_ p2) {
        Collection<Person_>  h,
                now1 = new HashSet<Person_>(),
                now2 = new HashSet<Person_>(),
                next = new HashSet<Person_>();
        int direction, label, n;

        Person_.resetAllLabels();
        if (p1 == null || p2 == null)
            return;
        if (p1 == p2) {
            p1.setLabel(1);
            path = new Person_[1];
            path[0] = p1;
            return;
        }

        p1.setLabel( 1); now1.add(p1);
        p2.setLabel(-1); now2.add(p2);

        while (true) {
            if (now1.isEmpty() || now2.isEmpty())
                return;

            if (now2.size() < now1.size()) {
                h = now1; now1 = now2; now2 = h;
            }

            Iterator<Person_> nowI = now1.iterator();
            while (nowI.hasNext()) {
                Person_ pnow = nowI.next();
                label = pnow.getLabel();
                direction = Integer.signum(label);
                Person_ neighbours[] = pnow.getCoauthors();
                int i;
                for (i=0; i<neighbours.length; i++) {
                    Person_ px = neighbours[i];
                    if (px.hasLabel()) {
                        if (Integer.signum(px.getLabel())
                                ==direction)
                            continue;
                        if (direction < 0) {
                            Person_ ph;
                            ph = px; px = pnow; pnow = ph;
                        }
                        // pnow has a positive label,
                        // px a negative                       
                        n = pnow.getLabel() - px.getLabel();
                        path = new Person_[n];
                        path[pnow.getLabel()-1] = pnow;
                        path[n+px.getLabel()] = px;
                        tracing(pnow.getLabel()-1);
                        tracing(n+px.getLabel());
                        return;
                    }
                    px.setLabel(label+direction);
                    next.add(px);
                }
            }
            now1.clear(); h = now1; now1 = next; next = h;
        }
    }
}