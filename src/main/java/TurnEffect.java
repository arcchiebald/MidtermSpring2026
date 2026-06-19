class TurnEffect {
    private final int advanceCount;
    private final int drawCount;
    private final boolean reverse;

    TurnEffect(int advanceCount, int drawCount, boolean reverse) {
        this.advanceCount = advanceCount;
        this.drawCount = drawCount;
        this.reverse = reverse;
    }

    int getAdvanceCount() {
        return advanceCount;
    }

    int getDrawCount() {
        return drawCount;
    }

    boolean isReverse() {
        return reverse;
    }
}
