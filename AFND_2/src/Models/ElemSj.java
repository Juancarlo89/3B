package Models;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

public class ElemSj {
    private int id;
    private Set<Estado> S;

    public ElemSj(){
        this.id = -1;
        this.S = new HashSet<>();
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Set<Estado> getS() { return S; }
    public void setS(Set<Estado> s) {
        this.S = Objects.requireNonNull(s, "El conjunto S no puede ser null");
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElemSj)) return false;
        return Objects.equals(S, ((ElemSj) o).S);
    }
    @Override
    public int hashCode() {
        return Objects.hash(S);
    }
    @Override
    public String toString() {
        String statesStr = (S == null) ? "null" : S.stream()
                                                    .filter(Objects::nonNull)
                                                    .map(Estado::getIdEdo)
                                                    .sorted()
                                                    .map(String::valueOf)
                                                    .collect(Collectors.joining(","));
        return "S" + id + "={" + statesStr + "}";
    }
}