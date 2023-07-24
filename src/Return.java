class Return extends RuntimeException {
    final Object returnVal;
    Return(Object val) {
        super(null, null, false, false);
        this.returnVal = val;
    }
}
