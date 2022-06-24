package script

type Script struct {
	header string
	loader string
}

func (s *Script) Header() string {
	return s.header
}

func (s *Script) Loader() string {
	return s.loader
}
