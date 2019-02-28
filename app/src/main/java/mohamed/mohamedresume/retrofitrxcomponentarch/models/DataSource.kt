package mohamed.mohamedresume.retrofitrxcomponentarch.models

data class DataSource(val source:String, val githubUserAdapter: ISourceAdapter)
interface ISourceAdapter