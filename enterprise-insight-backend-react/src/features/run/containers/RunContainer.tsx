import { useRunPage } from '../../../hooks/useRunPage'
import RunWorkspace from '../components/RunWorkspace'

export default function RunContainer() {
  const runPage = useRunPage()

  return <RunWorkspace {...runPage} />
}
